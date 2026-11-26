# Rapport de vérification : retirer les joueurs inactifs du classement général

- **Statut** : validé (cycle des statuts : `specs/README.md`)
- **Brief** : [1-brief.md](1-brief.md) · **Plan** : [2-plan.md](2-plan.md) · **Journal** : [3-execution.md](3-execution.md)
- **Commits vérifiés** : `origin/dev...8089d4d`

## Verdict

Prêt après la checklist ci-dessous. Tous les critères d'acceptation vérifiables côté API sont prouvés par les tests
et sur l'API lancée en local. Restent l'affichage dans kicker-app et le passage en CI, à faire par l'utilisateur.

## Preuves automatiques

| Commande | Résultat |
| -------- | -------- |
| `./mvnw -B test` (Docker démarré), avant correction | `BUILD SUCCESS`, 66 tests, 0 échec, 0 erreur, 0 ignoré ; `KickerMatchRepositoryTests` `Tests run: 6, Skipped: 0` |
| `./mvnw -B test` (Docker démarré), après la correction `8089d4d` | `BUILD SUCCESS`, 66 tests, 0 échec, 0 erreur, 0 ignoré ; `KickerMatchRepositoryTests` 6 tests, `KickerStatServiceDefaultTests` 7 tests, `Skipped: 0` |

## Critères d'acceptation

| Critère du brief | Preuve | Statut |
| ---------------- | ------ | ------ |
| Inactif depuis plus de 90 jours → `rank = 0`, `currentElo` gardé dès 10 matchs | `should_rank_only_active_players_with_enough_matches` (`inactive` : rang 0, ELO 1700) ; en local, Lewis rendu inactif : `rank 0`, `currentElo 2146` | ok |
| Rangs sans trou laissé par les inactifs | même test : rangs 1, 2, 3 malgré l'inactif à 1700 ; en local, rangs 1 à 18 sans trou (13, 13, 15 = égalité `RANK()`) avec l'ex-n° 1 inactif | ok |
| Un joueur qui rejoue retrouve un rang dès son match suivant | test (`returning` rang 2) ; en local, Lewis avec un match récent après 200 jours : rang 1 | ok |
| Pour ce joueur, `rankLastWeek = null` et `eloLastWeek` = ELO à la coupure | `should_rank_last_week_only_players_active_at_cutoff` (`returning` : rang `null`, ELO 1540) ; `should_fill_elo_last_week_without_rank_last_week_when_player_was_inactive_at_cutoff` ; en local : `rankLastWeek null`, `eloLastWeek 2162` (ELO après son dernier match avant lundi) | ok |
| `rankLastWeek` applique la règle d'activité à la coupure | `should_rank_last_week_only_players_active_at_cutoff`, `..._exactly_at_active_since` (borne incluse), `should_pass_active_since_90_days_before_cutoff_when_loading_last_week_ranking` | ok |
| `/season/{year}/{quarter}`, `/season`, `/player/{id}`, `/duo`, `/matrix-score` inchangés | `git diff origin/dev...HEAD` : aucune ligne dans `KickerEloSeasonalRepository`, `KickerEloRepository`, `EloMatrixServiceDefault`, ni hors des deux requêtes de `KickerMatchRepository` ; tous les hunks de `KickerStatServiceDefault` dans `getGlobalStats` (et imports, champ, constructeur). `LastKickerEloByDateProjection.getRank()` en `Integer` : la requête saisonnière renvoie toujours un rang. `/season` → `200` en local | ok |
| DTO de `/global` inchangé | `GlobalStatsWithHistoryDTO` absent du diff ; en local, mêmes 12 champs (`playerId` … `rankLastWeek`) | ok |
| Test des deux requêtes sur PostgreSQL, cinq cas du tableau | `KickerMatchRepositoryTests` (Testcontainers `postgres:17`, Flyway), jeu `insertBriefCases()` = les cinq cas, exécuté (`Skipped: 0`) | ok |

**Règles et cas limites** :

| Règle | Preuve | Statut |
| ----- | ------ | ------ |
| Borne des 90 jours incluse | deux tests `..._exactly_at_active_since` (maintenant et coupure), `>=` dans les deux requêtes | ok |
| Aucun joueur actif → tous à `rank = 0`, liste non vide | `should_return_all_players_unranked_when_nobody_is_active` | ok |
| Inactif avec moins de 10 matchs → `rank 0`, `currentElo null` | `newbie` dans `should_rank_only_active_players_with_enough_matches` (6 matchs, actif) ; même branche SQL (`total_matches < 10`) quel que soit l'actif | ok |
| Tout juste classé → `rankLastWeek` et `eloLastWeek` `null` | `justRanked` absent du résultat de lundi (`doesNotContainKeys`) ; `should_leave_last_week_values_empty_when_player_is_absent_at_cutoff` | ok |
| Match supprimé → redevient inactif au prochain appel | non testé dans cette feature : la vue est rafraîchie après suppression (code existant, non modifié) et la règle lit la vue | non vérifié (mécanisme existant) |
| `200`, ordre inchangé, liste vide sans joueur | `ORDER BY rank` inchangé ; en local `200` | ok |

**Tests par tâche** : chaque tâche de logique a son test, et ce test échouerait si la logique cassait. Sans le filtre
d'activité, `inactive` (1700) prendrait le rang 1 dans `should_rank_only_active_players_with_enough_matches`. Sans le
`LEFT JOIN`, `inactive` et `returning` disparaîtraient du résultat de lundi.

## Revue de code

Revue indépendante (sous-agent, contexte neuf) : aucun bug de résultat trouvé. Fuseaux vérifiés cohérents : `created_at`
est écrit en heure de la JVM, `activeSince` est calculé dans `ZoneId.systemDefault()`, et le `Clock` UTC ne fournit
qu'un instant. Aucun doublon possible dans `eloLastWeekMap`. Les cinq points de vigilance du plan sont OK.

- **Corrigés** :
    1. Nom pleinement qualifié `java.sql.Timestamp` dans la ligne ajoutée de `getGlobalStats` (`code-style.md`,
       « jamais de nom pleinement qualifié ») : import ajouté. Commit `8089d4d`
       `fix(kicker): import Timestamp instead of using its qualified name`, suite relancée verte. La ligne
       équivalente de `getSeasonStats` est laissée telle quelle (hors périmètre).
    2. Dette repérée mais pas reportée : ajoutée à `docs/tech-debt.md` (voir plus bas).
    3. `make test` ignore les tests PostgreSQL (le conteneur `tests` n'a pas accès à Docker) alors que la doc dit
       « lance Docker » : précisé dans `testing.md`, ajouté à `tech-debt.md`.
    4. Lignes « État » rendues fausses par la branche (« aucun `Clock` », « aucune lecture en `readOnly` ») : mises à
       jour dans `code-style.md`, `persistence.md` et `tech-debt.md`.
- **Non traités** : aucun.

## Vérification en conditions réelles

`make build` (l'image datait d'avant les commits de la feature), puis `make dev`, sur `localhost:8080`.

| Vérification | Résultat |
| ------------ | -------- |
| Démarrage | `GET /api/v1/ping` → `pong` |
| Migrations | `flyway_schema_history` : V19, V20 et le seed `V9999` appliqués, `success = t` ; aucune migration dans la feature |
| `GET /api/v1/kicker/stats/global` (données de dev) | `200`, 27 joueurs, 19 classés de 1 à 19 (égalité 14, 14, 16), 8 non classés à `rank 0` / `currentElo null`. Les 19 joueurs à 10 matchs ou plus sont tous actifs dans le seed : aucun inactif à l'état initial |
| Cas inactif (données locales modifiées) | matchs de Lewis (n° 1, ELO 2146) reculés de 200 jours, vue rafraîchie : `rank 0`, `currentElo 2146`, `rankLastWeek null`, `eloLastWeek 2146` ; autres rangs 1 à 18 sans trou |
| Cas joueur qui revient | son dernier match remis à sa date (2026-11-24, après la coupure) : `rank 1`, `rankLastWeek null`, `eloLastWeek 2162` (ELO avant ce match) |
| Remise en état | dates restaurées, vue rafraîchie : réponse de `/global` identique octet pour octet à l'état initial |
| `GET /api/v1/kicker/stats/season` | `200` |
| Endpoint protégé sans jeton (`POST /api/v1/kicker/matches`) | `401` (aucun endpoint protégé touché) |

Remarque : `make dev` (`docker compose up -d`) démarre aussi le service `tests` (`mvn test` sur le dossier monté). Ce
comportement existait déjà ; il a fini en `Exited (0)` avant la seconde passe de `./mvnw`.

## À faire par l'utilisateur

- [ ] **Visiteur (public)** sur kicker-app (branche `feat/hide-inactive-players`) : un joueur à `rank 0` avec un
      `currentElo` s'affiche comme « inactif », et non comme « moins de 10 matchs ». Un joueur classé avec
      `rankLastWeek = null` et `eloLastWeek` renseigné affiche son évolution d'ELO, sans badge « nouveau classé ». Les
      `rank 0` sont en tête de la réponse (ordre inchangé) : le front doit les trier ou les filtrer comme prévu dans son
      brief. Pour les faire apparaître en local, reculer les matchs d'un joueur (voir le tableau ci-dessus).
- [ ] **CI** : après le push, vérifier dans le job `tests.yml` (`ubuntu-latest`) que `KickerMatchRepositoryTests`
      affiche `Skipped: 0` avec Testcontainers 1.21.4.
- [ ] **Production** : la règle suppose que la JVM écrit `created_at` et calcule `activeSince` dans le même fuseau
      (UTC dans l'image Docker, cohérent). Aucun changement de `TZ` à prévoir.

Données de test : aucune laissée en base locale (modification annulée, réponse identique à l'état initial).

## Dette et documentation

- **Ajouté à `docs/tech-debt.md`** :
    - P1 : coupure « semaine dernière » construite avec `Calendar.getInstance()`, qui dépend de la locale (le dimanche,
      elle peut tomber sur le lundi suivant) ;
    - P1 : `lastMatches` d'un classement de saison non filtré par saison ;
    - Outillage : `make test` ignore les tests PostgreSQL ;
    - Outillage : les tests de couche H2 lisent le `config/application.yml` local (décision de la tâche 2 du journal).
    - La N+1 de `lastMatches` était déjà listée.
- **Retiré de `docs/tech-debt.md`** : rien de retiré. Lignes précisées : « aucun `Clock` » devient « `Clock`
  seulement dans `getGlobalStats` » ; « aucune lecture en `readOnly` » devient « `readOnly` seulement sur
  `getGlobalStats` ».
- **Doc mise à jour** : `docs/conventions/testing.md` (`make test` ignore les tests PostgreSQL),
  `docs/conventions/code-style.md` et `docs/conventions/persistence.md` (lignes « État »). Contrôle de l'étape
  « Documenter » : l'endpoint est décrit dans `architecture/elo-and-stats.md` (mis à jour en tâche 3, `api.md` y
  renvoie), le glossaire et l'ADR 0007 sont faits, la sécurité et la configuration sont inchangées, aucun `shortcut:`
  dans le diff, tous les fichiers touchés font moins de 200 lignes.
