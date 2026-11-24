# Brief : retirer les joueurs inactifs du classement général

- **Statut** : validé (cycle des statuts : `specs/README.md`)
- **Branche** : `feat/hide-inactive-players`
- **Date** : 2026-11-24
- **Front lié** : kicker-app, branche `feat/hide-inactive-players`, `specs/hide-inactive-players/1-brief.md`

## Besoin

Le classement général (`GET /api/v1/kicker/stats/global`) classe tous les joueurs qui ont au moins 10 matchs depuis le
début, même s'ils ne jouent plus depuis des mois. Ces joueurs figés occupent des places et faussent la lecture du
classement. On veut qu'un joueur qui ne joue plus sorte du classement général, sans perdre son ELO ni son historique,
et qu'il y revienne dès qu'il rejoue.

## Solution retenue

`GET /api/v1/kicker/stats/global` ne classe plus que les joueurs **actifs** (au moins un match dans les 90 derniers
jours) qui ont au moins 10 matchs. Les autres restent dans la réponse avec `rank = 0`. Le contrat (DTO, ordre, droits)
ne change pas : seules les valeurs de `rank`, `rankLastWeek` et `eloLastWeek` changent.

## Rôles et accès

| Rôle                      | Voit                       | Peut faire | Est refusé |
| ------------------------- | -------------------------- | ---------- | ---------- |
| Tous (public, sans jeton) | le classement général      | lire       | —          |

Droits inchangés : l'endpoint reste public.

## Scénarios

1. En tant que visiteur, je veux un classement général qui ne montre que les joueurs qui jouent encore, afin de lire
   un classement fidèle au niveau actuel.
2. En tant que joueur revenu après une longue absence, je veux réapparaître au classement dès mon premier match, avec
   mon gain ou ma perte d'ELO depuis lundi, afin de ne pas être présenté comme un nouveau classé.

## Données et contrats

- **Endpoint** : `GET /api/v1/kicker/stats/global` uniquement (existant, modifié dans ses valeurs).
- **DTO** : `GlobalStatsWithHistoryDTO` **inchangé**. Aucun champ ajouté ni retiré.
- **Ordre de la réponse** : inchangé.
- **kicker-app** : le front lit `rank = 0` + `currentElo` renseigné comme « inactif » (voir son brief). Aucune rupture.
- **Données lues** : les matchs, via `mv_player_match_facts` (date du dernier match par joueur). Aucune écriture,
  aucune migration.

## Règles métier et cas limites

### Vocabulaire

- **Joueur actif** : un joueur qui a joué au moins un match dans les 90 jours précédant la date de référence.
- **Classé** (classement général) : au moins 10 matchs au total (`MIN_RANKED_MATCHES`, inchangé) **et** actif.
- **Non classé** : tous les autres joueurs. Ils restent dans la réponse, avec `rank = 0`.

### Classement actuel (`rank`, `currentElo`)

- **Date de référence** : maintenant.
- **`rank`** : le `RANK()` actuel (par ELO global décroissant) est calculé **uniquement** parmi les joueurs classés
  (au moins 10 matchs **et** actifs). Les autres ont `rank = 0`. Les inactifs ne laissent donc pas de trou dans la
  numérotation. Les égalités restent gérées comme aujourd'hui (`RANK()` : 1, 1, 3).
- **`currentElo`** : **inchangé**. Il est renvoyé dès que le joueur a au moins 10 matchs, actif ou non, et vaut `null`
  en dessous. Un joueur inactif avec 10 matchs ou plus a donc `rank = 0` **et** un `currentElo` renseigné : c'est ce qui
  permet au front de le distinguer d'un joueur qui a moins de 10 matchs.

### Classement de référence (`rankLastWeek`, `eloLastWeek`)

- **Date de référence** : la coupure actuelle (lundi 00:00 de la semaine en cours), inchangée.
- **`rankLastWeek`** : même règle que `rank`, évaluée à la coupure. Le `RANK()` est calculé parmi les joueurs qui
  avaient, à la coupure, au moins 10 matchs **et** au moins un match dans les 90 jours avant la coupure. Pour les
  autres, `null` (comme aujourd'hui pour les joueurs sous le seuil).
- **`eloLastWeek`** : **découplé du rang**. Il vaut l'ELO après le dernier match avant la coupure pour **tout** joueur
  qui avait au moins 10 matchs à la coupure, **actif ou non** à cette date. Sinon, `null`.

### Comportement attendu (date de référence : maintenant)

| Joueur                                               | `rank` | `currentElo` | `rankLastWeek` | `eloLastWeek` |
| ---------------------------------------------------- | ------ | ------------ | -------------- | ------------- |
| 50 matchs, dernier il y a 3 jours                    | > 0    | ELO          | rang de lundi  | ELO de lundi  |
| 50 matchs, dernier il y a 120 jours                  | 0      | ELO          | `null`         | ELO de lundi  |
| 50 matchs, revenu hier après 6 mois sans jouer       | > 0    | ELO          | `null`         | ELO de lundi  |
| 6 matchs, dernier hier                               | 0      | `null`       | `null`         | `null`        |
| 9 matchs lundi, 10e match hier (vient d'être classé) | > 0    | ELO          | `null`         | `null`        |

### Cas limites

- **Limite des 90 jours** : un match joué exactement à la borne compte (borne incluse : `match_date >= activeSince`).
- **Aucun joueur actif** : tous les joueurs ont `rank = 0`. La réponse n'est pas vide.
- **Joueur inactif avec moins de 10 matchs** : `rank = 0`, `currentElo = null`. Il ne se distingue pas d'un nouveau
  joueur, et c'est voulu.
- **Match supprimé** : si c'était le seul match récent d'un joueur, il redevient inactif au prochain appel (la vue
  matérialisée est déjà rafraîchie après une suppression).

### Pistes d'implémentation

À titre indicatif, d'après la lecture du code (`kicker_match_service`) :

- **Constante** : ajouter `ACTIVE_PLAYER_WINDOW_DAYS = 90` à côté de `MIN_RANKED_MATCHES`, dans
  `service/KickerEloVisibilityRules.java`.
- **Requêtes** : modifier les deux requêtes du classement général dans `repository/KickerMatchRepository.java` :
  - `getGlobalKickerStats` : filtre d'activité dans le `RANK()` ;
  - `getLatestKickerEloByDate` : filtre d'activité pour `rankLastWeek` uniquement. L'ELO de lundi est renvoyé pour
    tout joueur au-dessus du seuil.

  Le dernier match d'un joueur se lit avec `MAX(match_date)` dans `mv_player_match_facts`, qui a déjà l'index
  `(player_id, match_date DESC)`.
- **Fuseau horaire** : `created_at` est un `TIMESTAMP` sans fuseau, écrit avec l'heure de la JVM. Calculer la borne
  « référence moins 90 jours » côté Java et la passer en paramètre (`:activeSince`), plutôt que d'utiliser `now()` en
  SQL, pour rester cohérent avec l'écriture.
- **Service** : `service/impl/KickerStatServiceDefault.getGlobalStats` alimente aujourd'hui `rankLastWeek` et
  `eloLastWeek` avec une seule requête qui filtre déjà sur le seuil. Il faudra soit deux résultats, soit une requête
  qui renvoie le rang (éventuellement `null`) et l'ELO séparément.

## États et erreurs

- **Succès** : `200` avec la liste de tous les joueurs, classés ou non, dans le même ordre qu'aujourd'hui.
- **Vide** : sans joueur, liste vide comme aujourd'hui. Sans joueur actif, liste complète avec `rank = 0` partout.
- **Erreur** : aucune nouvelle erreur. L'endpoint n'a pas de paramètre ; une panne de base reste un `500`.

## Tests

- Test unitaire du service : `rankLastWeek` et `eloLastWeek` alimentés séparément (cas du joueur revenu après plus de
  90 jours).
- Les deux requêtes du classement général sont des requêtes natives PostgreSQL que H2 ne sait pas exécuter : un test
  sur PostgreSQL (Testcontainers ou équivalent) couvre au minimum les cinq cas du tableau ci-dessus.
- Aucun nouveau test MockMvc : le contrat de l'endpoint ne change pas.

## Critères d'acceptation

- [ ] Un joueur sans match depuis plus de 90 jours a `rank = 0` dans `/global`, et son `currentElo` reste renseigné
      s'il a au moins 10 matchs.
- [ ] Les rangs de `/global` se suivent sans trou laissé par les inactifs (hors égalités).
- [ ] Un joueur qui rejoue après plus de 90 jours retrouve un rang dès son match suivant.
- [ ] Pour ce joueur, `rankLastWeek = null` et `eloLastWeek` vaut son ELO à la coupure de lundi.
- [ ] `rankLastWeek` applique la règle d'activité évaluée à la coupure de lundi.
- [ ] Les endpoints `/season/{year}/{quarter}`, `/season`, `/player/{id}`, `/duo` et `/matrix-score` renvoient
      exactement la même chose qu'avant.
- [ ] Le DTO de `/global` est inchangé.
- [ ] Un test couvre les deux requêtes du classement général sur PostgreSQL. Les tests actuels tournent sur H2, qui ne
      sait pas exécuter ces requêtes (Testcontainers ou équivalent). Il couvre au minimum les cinq cas du tableau
      ci-dessus.

## Hors périmètre

- Les classements saisonniers (`/season/{year}/{quarter}`) : un non classé y reste un joueur avec moins de 10 matchs
  dans la saison.
- La constante `MIN_RANKED_MATCHES` et ses autres usages : historique ELO de la fiche joueur, ELO affiché dans la
  liste des matchs.
- Le calcul de l'ELO : les matchs des joueurs inactifs comptent toujours.
- Une notion de joueur archivé ou masqué.
- Signalé au passage, non traité ici (candidats pour `docs/tech-debt.md`) :
  - **Coupure du lundi** : elle est construite avec `Calendar.getInstance()`, qui dépend de la locale du serveur. Avec
    une locale où la semaine commence le dimanche, un dimanche, la coupure tombe le lundi **suivant**. Les valeurs
    « de la semaine dernière » sont alors égales aux valeurs actuelles.
  - **N+1** : `lastMatches` est chargé par une requête par joueur dans `getGlobalStats` et `getSeasonStats`.
  - **`lastMatches` sur une saison** : il n'est pas filtré par saison. L'endpoint d'une saison renvoie les 5 derniers
    matchs tous temps confondus.

## Décisions

- **Fenêtre de 90 jours, glissante**, évaluée à chaque date de référence (maintenant, coupure de lundi).
- **`rank = 0` plutôt que retirer le joueur de la réponse** : le contrat reste inchangé et le front garde la main sur
  l'affichage des inactifs.
- **`currentElo` gardé pour un inactif classable** : c'est le signal qui distingue « inactif » de « moins de 10
  matchs », sans nouveau champ.
- **`eloLastWeek` découplé de `rankLastWeek`** : un joueur qui revient doit voir son évolution depuis lundi au lieu
  d'apparaître comme un nouveau classé.
- **Borne calculée côté Java** (`:activeSince`) : cohérente avec l'écriture des dates en heure de la JVM.
- Aucun ADR : la règle se change facilement (une constante et deux requêtes).
- **Test PostgreSQL par Testcontainers** (`postgres:17`, migrations Flyway), **ignoré sans Docker**
  (`disabledWithoutDocker`) : `./mvnw -B test` reste vert sans Docker, et le test tourne en CI. Décision structurante
  consignée dans l'ADR 0007. _Ajouté à la planification._

## Glossaire

- **Joueur actif** : ajouté.
- **Joueur classé** : précisé (règle du classement général : 10 matchs et actif ; saison inchangée), mention
  « _À confirmer_ » retirée.
