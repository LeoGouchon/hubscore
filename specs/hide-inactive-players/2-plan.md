# Plan : retirer les joueurs inactifs du classement général

- **Statut** : validé (cycle des statuts : `specs/README.md`)
- **Brief** : [1-brief.md](1-brief.md)
- **Exécution** : dans la conversation (3 tâches)

**Objectif** : `GET /api/v1/kicker/stats/global` ne classe plus que les joueurs actifs (au moins un match dans les
90 derniers jours) ayant au moins 10 matchs, et découple `eloLastWeek` de `rankLastWeek`.

**Approche** : on ajoute un paramètre `activeSince` (calculé côté Java) aux deux requêtes natives du classement
général, qui filtrent le `RANK()` sur la date du dernier match lue dans `mv_player_match_facts`. Le rang de lundi
devient nullable dans sa projection, ce qui suffit au service pour remplir `eloLastWeek` sans `rankLastWeek`. Les
requêtes sont testées sur un vrai PostgreSQL par Testcontainers, introduit à la tâche 1.

## Contraintes globales

- Fenêtre d'activité : `ACTIVE_PLAYER_WINDOW_DAYS = 90`, borne **incluse** (`match_date >= :activeSince`).
- Seuil de classement : `MIN_RANKED_MATCHES = 10`, inchangé, ni sa valeur ni ses autres usages.
- `activeSince` = date de référence moins 90 jours, en **`LocalDateTime` dans le fuseau de la JVM** : `created_at`
  est un `TIMESTAMP` sans fuseau écrit avec l'heure de la JVM (`new Timestamp(System.currentTimeMillis())`), la borne
  doit l'être aussi. Un commentaire d'une ligne le dit à l'endroit du calcul (écart déjà listé dans `tech-debt.md`,
  ligne « Formats de date mélangés… colonnes `TIMESTAMP` sans fuseau »).
- « Maintenant » vient d'un `Clock` injecté (bean `Clock.systemUTC()`), jamais de `System.currentTimeMillis()` dans le
  code modifié.
- Contrat inchangé : `GlobalStatsWithHistoryDTO` sans nouveau champ, `ORDER BY rank` de `getGlobalKickerStats`
  conservé tel quel, endpoint public, aucune nouvelle erreur.
- Hors périmètre, **ne pas toucher** : `getSeasonStats`, `KickerEloSeasonalRepository`, `getPlayerStats`,
  `getDuoStats`, la construction de la coupure du lundi par `Calendar` (sauf sa source de temps, tâche 2), la N+1 de
  `lastMatches`.
- Tests : unitaire Mockito/AssertJ pour le service, test PostgreSQL pour les requêtes ; aucun test MockMvc (contrat
  inchangé). Noms `should_<comportement>_when_<condition>`.
- Preuve : `./mvnw -B test` → `BUILD SUCCESS`, 0 échec, 0 erreur ; **avec Docker lancé**, le test PostgreSQL doit
  apparaître exécuté (`Skipped: 0` pour `KickerMatchRepositoryTests`).

## Points de vigilance

1. Joueur dont le dernier match tombe **exactement** sur `activeSince` → classé (vérifié en tâches 2 et 3).
2. **Aucun joueur actif** → tous les joueurs présents avec `rank = 0`, liste non vide (vérifié en tâche 2).
3. Joueur inactif avec l'**ELO le plus haut** → les classés sont numérotés 1, 2, 3 sans trou (vérifié en tâche 2 ; même
   chose à la coupure en tâche 3).
4. Joueur **tout juste classé** (9 matchs à la coupure, 10e après) → absent du résultat de lundi, donc
   `rankLastWeek = null` **et** `eloLastWeek = null` (vérifié en tâche 3).
5. Les classements **saisonniers** renvoient la même chose qu'avant → aucune ligne modifiée dans
   `KickerEloSeasonalRepository` ni dans `getSeasonStats` (vérifié par `git diff` en tâche 3).

## Carte des fichiers

| Fichier                                                                                   | Action   | Responsabilité                                                    |
| ----------------------------------------------------------------------------------------- | -------- | ----------------------------------------------------------------- |
| `pom.xml`                                                                                 | modifier | dépendances de test Testcontainers                                |
| `src/test/resources/application-postgres.yml`                                             | créer    | profil de test PostgreSQL : Flyway actif, dialecte PostgreSQL     |
| `src/test/java/.../kicker_match_service/repository/KickerMatchRepositoryTests.java`       | créer    | tests des requêtes natives sur PostgreSQL (conteneur, jeu de données) |
| `src/main/java/.../common/config/ClockConfig.java`                                        | créer    | bean `Clock`                                                      |
| `src/main/java/.../kicker_match_service/service/KickerEloVisibilityRules.java`            | modifier | constante `ACTIVE_PLAYER_WINDOW_DAYS`                             |
| `src/main/java/.../kicker_match_service/repository/KickerMatchRepository.java`            | modifier | filtre d'activité dans les deux requêtes du classement général    |
| `src/main/java/.../kicker_match_service/repository/projection/LastKickerEloByDateProjection.java` | modifier | `getRank()` nullable                                     |
| `src/main/java/.../kicker_match_service/service/impl/KickerStatServiceDefault.java`       | modifier | `Clock`, calcul des deux `activeSince`                            |
| `src/test/java/.../kicker_match_service/service/KickerStatServiceDefaultTests.java`       | modifier | constructeur avec `Clock`, tests de `getGlobalStats`              |
| `docs/decisions/0007-tests-postgresql-testcontainers.md`, `docs/decisions/README.md`      | créer / modifier | ADR et sommaire                                           |
| `docs/conventions/testing.md`, `docs/getting-started.md`, `CLAUDE.md`, `.claude/workflow.md`, `docs/tech-debt.md` | modifier | mention des tests PostgreSQL ignorés sans Docker |
| `docs/architecture/elo-and-stats.md`                                                      | modifier | règle d'activité du classement général                           |

`...` = `com/leogouchon/hubscore`.

---

### Tâche 1 : infrastructure de test PostgreSQL (Testcontainers)

**Fichiers** :

- Modifier : `pom.xml`
- Créer : `src/test/resources/application-postgres.yml`
- Créer : `src/test/java/com/leogouchon/hubscore/kicker_match_service/repository/KickerMatchRepositoryTests.java`
- Créer : `docs/decisions/0007-tests-postgresql-testcontainers.md`
- Modifier : `docs/decisions/README.md`, `docs/conventions/testing.md`, `docs/getting-started.md`, `CLAUDE.md`,
  `.claude/workflow.md`, `docs/tech-debt.md`

**Interfaces** :

- Fournit : la classe `KickerMatchRepositoryTests` (conteneur, profil, `JdbcTemplate`, `KickerMatchRepository`
  injectés) que les tâches 2 et 3 complètent.

- [ ] **Étape 1** : dans `pom.xml`, ajouter en scope `test`, sans version (gérées par le parent Spring Boot 3.4.2) :
      `org.springframework.boot:spring-boot-testcontainers`, `org.testcontainers:junit-jupiter`,
      `org.testcontainers:postgresql`.
- [ ] **Étape 2** : créer `application-postgres.yml`, chargé **en plus** du profil `test` :
      `spring.flyway.enabled: true`, `spring.jpa.hibernate.ddl-auto: none`,
      `spring.jpa.database-platform: org.hibernate.dialect.PostgreSQLDialect`,
      `spring.datasource.driver-class-name: org.postgresql.Driver`. L'URL vient de `@ServiceConnection`.
- [ ] **Étape 3** : créer `KickerMatchRepositoryTests`, annotée `@DataJpaTest`,
      `@AutoConfigureTestDatabase(replace = Replace.NONE)`, `@ActiveProfiles({"test", "postgres"})`,
      `@Testcontainers(disabledWithoutDocker = true)`, avec
      `@Container @ServiceConnection static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")`
      (même version que `docker-compose.yml`). Les migrations de `db/migration` s'appliquent ; le seed de
      `db/dev-data` n'est pas chargé.
- [ ] **Étape 4** : test `should_apply_flyway_migrations_when_postgres_starts` : `SELECT COUNT(*) FROM
      mv_player_match_facts` renvoie `0` (la vue existe, base vide).
- [ ] **Étape 5** : ADR `0007-tests-postgresql-testcontainers.md` (gabarit `0000-template.md`, statut Accepté, date
      2026-11-24) : contexte (H2 ne joue ni migrations, ni vue, ni SQL natif ; ADR 0003) ; options (Testcontainers
      ignoré sans Docker / obligatoire / vérification manuelle) ; décision : Testcontainers ignoré sans Docker ;
      conséquences : `./mvnw -B test` reste vert sans Docker, la CI (Docker sur `ubuntu-latest`) les exécute, un
      développeur sans Docker ne les voit pas tourner ; une CI sans Docker les sauterait sans échouer. Ajouter
      l'entrée au sommaire `docs/decisions/README.md` avec sa ligne « À lire si » (tu écris un test de requête native
      ou de migration, ou tu touches à l'infrastructure de test).
- [ ] **Étape 6** : doc :
    - `testing.md`, section « Tests de requêtes » : remplacer « Limite actuelle » par la recette (classe annotée comme
      à l'étape 3, profils `test` + `postgres`, données insérées en SQL puis `REFRESH MATERIALIZED VIEW`), lien vers
      l'ADR 0007 ; fichier clé `application-postgres.yml` ;
    - `getting-started.md` (lignes 13 et 70) et `CLAUDE.md` (commande `./mvnw -B test`) : « H2 en mémoire ; les tests
      PostgreSQL (Testcontainers) sont ignorés sans Docker » ;
    - `.claude/workflow.md` : même précision dans **Preuves automatiques** ; dans **Points de revue**, « requête
      native vérifiée sur PostgreSQL : test Testcontainers, sinon `make dev` » ;
    - `tech-debt.md`, ligne « Tests sur H2 » : l'infrastructure existe (ADR 0007) ; reste à couvrir les autres
      requêtes natives et la vue.
- [ ] **Étape 7** : vérifier : `./mvnw -B test` → `BUILD SUCCESS` ; Docker lancé, le rapport de
      `KickerMatchRepositoryTests` indique `Tests run: 1, Skipped: 0`. Si Docker n'est pas disponible, le noter dans le
      journal (test ignoré) et le signaler.
- [ ] **Étape 8** : commit `test(kicker): add Testcontainers PostgreSQL setup for native queries`.

### Tâche 2 : classement actuel limité aux joueurs actifs

**Fichiers** :

- Créer : `src/main/java/com/leogouchon/hubscore/common/config/ClockConfig.java`
- Modifier : `KickerEloVisibilityRules.java`, `KickerMatchRepository.java`, `KickerStatServiceDefault.java`
- Tester : `KickerMatchRepositoryTests.java`, `KickerStatServiceDefaultTests.java`

**Interfaces** :

- Utilise : `KickerMatchRepositoryTests` (tâche 1).
- Fournit :
    - `KickerEloVisibilityRules.ACTIVE_PLAYER_WINDOW_DAYS = 90` (`public static final int`) ;
    - `@Bean Clock clock()` → `Clock.systemUTC()` dans `ClockConfig` (`@Configuration`) ;
    - constructeur `KickerStatServiceDefault(KickerMatchRepository, KickerEloSeasonalRepository, PlayerService,
      KickerEloRepository, Clock)` ;
    - `List<GlobalStatsResponseProjection> getGlobalKickerStats(Integer year, Integer quarter, int minRankedMatches,
      LocalDateTime activeSince)` (`@Param("activeSince")`) ;
    - fabriques de test privées de `KickerMatchRepositoryTests`, réutilisées en tâche 3 :
      `UUID insertPlayer(String firstname, int currentElo)` (ligne `players` + `player_kicker_informations`) ;
      `void insertMatch(UUID player, LocalDateTime playedAt, int eloAfter)` : match 1 contre 1 contre un adversaire
      **créé pour ce match**, avec les lignes `kicker_matches` (`created_at = playedAt`), `kicker_elo` et
      `kicker_elo_seasonal` **du seul joueur suivi** (l'adversaire n'entre donc pas dans la vue) ;
      `void insertMatches(UUID player, int count, LocalDateTime lastPlayedAt, int eloAfterLast)` : `count` matchs à un
      jour d'intervalle, le dernier à `lastPlayedAt` ; `void refreshFacts()` → `REFRESH MATERIALIZED VIEW
      mv_player_match_facts` ; `BriefCases insertBriefCases()` qui insère le jeu de l'étape 5 et renvoie le
      `record BriefCases(UUID active, UUID inactive, UUID returning, UUID newbie, UUID justRanked)`. Colonnes
      exactes : lire les migrations `V1` à `V20`.

- [ ] **Étape 1** : ajouter `ACTIVE_PLAYER_WINDOW_DAYS` et créer `ClockConfig`.
- [ ] **Étape 2** : `getGlobalKickerStats` : dans `player_stats`, ajouter `MAX(match_date) AS last_match_date` ; dans
      `ranked_players`, ajouter `AND ps.last_match_date >= :activeSince` au `WHERE`. `currentElo`, `COALESCE(rp.rank,
      0)` et `ORDER BY rank` inchangés.
- [ ] **Étape 3** : `KickerStatServiceDefault` : `Clock` en dernier paramètre du constructeur ;
      `@Transactional(readOnly = true)` (Spring) sur `getGlobalStats` ; `getGlobalStats` passe
      `LocalDateTime.ofInstant(clock.instant(), ZoneId.systemDefault()).minusDays(ACTIVE_PLAYER_WINDOW_DAYS)` en
      `activeSince`, et la coupure du lundi part de `c.setTimeInMillis(clock.millis())` au lieu de
      `System.currentTimeMillis()` (le reste du `Calendar` inchangé). `getSeasonStats` n'est pas modifié.
- [ ] **Étape 4** : `KickerStatServiceDefaultTests` : `serviceForDate` passe un `Clock` ; test
      `should_pass_active_since_90_days_before_now_when_loading_global_ranking` : `Clock.fixed(Instant.parse(
      "2026-11-25T11:00:00Z"), ZoneOffset.UTC)`, vérifie l'appel `getGlobalKickerStats(null, null, 10,
      LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).minusDays(90))`.
- [ ] **Étape 5** : tests PostgreSQL, référence `NOW = 2026-11-25T12:00`, `activeSince = NOW.minusDays(90)` passés
      directement à la requête (`year` et `quarter` à `null`, `minRankedMatches = 10`) :
    - `should_rank_only_active_players_with_enough_matches` : jeu de données des cinq cas du brief, ELO actuel entre
      parenthèses — `active` 11 matchs, dernier `NOW-3j` (1600) ; `inactive` 10 matchs, dernier `NOW-120j` (1700) ;
      `returning` 10 matchs finissant à `NOW-180j` puis 1 à `NOW-1j` (1550) ; `newbie` 6 matchs, dernier `NOW-1j`
      (1400) ; `justRanked` 9 matchs finissant à `NOW-10j` puis 1 à `NOW-1j` (1500). Attendu : `active` rang 1,
      `returning` rang 2, `justRanked` rang 3 (aucun trou malgré `inactive` à 1700) ; `inactive` rang 0 et
      `currentElo = 1700` ; `newbie` rang 0 et `currentElo = null`.
    - `should_rank_player_when_last_match_is_exactly_at_active_since` : 10 matchs, dernier à `activeSince` pile →
      rang 1.
    - `should_return_all_players_unranked_when_nobody_is_active` : deux joueurs de 10 matchs, derniers à `NOW-100j`
      → deux lignes (plus les adversaires), toutes `rank = 0`, `currentElo` renseigné.
- [ ] **Étape 6** : vérifier : `./mvnw -B test` → `BUILD SUCCESS`, Docker lancé : `KickerMatchRepositoryTests`
      `Skipped: 0`.
- [ ] **Étape 7** : commit `feat(kicker): rank only active players in global ranking`.

### Tâche 3 : classement de lundi limité aux actifs, ELO de lundi découplé

**Fichiers** :

- Modifier : `KickerMatchRepository.java`, `LastKickerEloByDateProjection.java`, `KickerStatServiceDefault.java`,
  `docs/architecture/elo-and-stats.md`
- Tester : `KickerMatchRepositoryTests.java`, `KickerStatServiceDefaultTests.java`

**Interfaces** :

- Utilise : fabriques de test, constante et `Clock` de la tâche 2.
- Fournit : `List<LastKickerEloByDateProjection> getLatestKickerEloByDate(Timestamp date, int minRankedMatches,
  LocalDateTime activeSince)` ; `Integer LastKickerEloByDateProjection.getRank()` (au lieu de `int`), `null` pour un
  joueur au-dessus du seuil mais inactif à la coupure.

- [ ] **Étape 1** : `getLatestKickerEloByDate` : `eligible_players` garde `lpp.match_date AS last_match_date` (le
      dernier match avant la coupure, `rn = 1`) ; `ranked_players` calcule `RANK() OVER (ORDER BY elo_after_match
      DESC)` sur `eligible_players WHERE last_match_date >= :activeSince` ; le `SELECT` final part de
      `eligible_players` (tous les joueurs au-dessus du seuil) en `LEFT JOIN ranked_players`, renvoie `elo` et `rank`
      (nullable), `ORDER BY rank NULLS LAST`. Le filtre `match_date <= :date` est inchangé.
- [ ] **Étape 2** : projection : `Integer getRank()`. Le service garde `dto.setEloLastWeek(lastWeek.getElo())` et
      `dto.setRankLastWeek(lastWeek.getRank())` : la valeur `null` passe telle quelle.
- [ ] **Étape 3** : service : la coupure `Timestamp` est passée inchangée, et `activeSince =
      cutoff.toLocalDateTime().minusDays(ACTIVE_PLAYER_WINDOW_DAYS)`.
- [ ] **Étape 4** : tests unitaires du service (`Clock.fixed` comme en tâche 2, `getLastFiveResultsByPlayerId` →
      `List.of()`) :
    - `should_pass_active_since_90_days_before_cutoff_when_loading_last_week_ranking` : capture `date` et
      `activeSince` ; `activeSince == date.toLocalDateTime().minusDays(90)`, `date` un lundi à `00:00`.
    - `should_fill_elo_last_week_without_rank_last_week_when_player_was_inactive_at_cutoff` : actuel `rank = 1`,
      `currentElo = 1580` ; lundi `elo = 1550`, `rank = null` → `rank = 1`, `eloLastWeek = 1550`,
      `rankLastWeek = null`.
    - `should_fill_rank_last_week_when_player_was_ranked_at_cutoff` : lundi `elo = 1550`, `rank = 2` →
      `rankLastWeek = 2`, `eloLastWeek = 1550`.
    - `should_leave_last_week_values_empty_when_player_is_absent_at_cutoff` : rien pour ce joueur lundi → les deux
      `null`.
- [ ] **Étape 5** : tests PostgreSQL, `cutoff = 2026-11-23T00:00` (lundi), `activeSince = cutoff.minusDays(90)` :
    - `should_rank_last_week_only_players_active_at_cutoff` : `insertBriefCases()` (tâche 2), dont l'ELO après le
      dernier match avant la coupure vaut — `active` 1590, `inactive` 1700, `returning` 1540 (à fixer dans
      `insertBriefCases` dès la tâche 2) ; `justRanked` a 9 matchs avant la coupure.
      Attendu : `active` `rank = 1`, `elo = 1590` ; `inactive` `rank = null`, `elo = 1700` ; `returning`
      `rank = null`, `elo = 1540` ; `newbie` et `justRanked` absents.
    - `should_rank_last_week_player_when_last_match_is_exactly_at_active_since` : 10 matchs, dernier pile à
      `cutoff.minusDays(90)` → `rank = 1`.
- [ ] **Étape 6** : `docs/architecture/elo-and-stats.md`, section « Visibilité de l'ELO » : le classement général ne
      classe que les joueurs qui ont 10 matchs **et** un match dans les 90 derniers jours (`ACTIVE_PLAYER_WINDOW_DAYS`) ;
      les autres sont renvoyés avec `rank = 0`, `currentElo` gardé dès 10 matchs ; même règle à la coupure du lundi pour
      `rankLastWeek`, `eloLastWeek` renvoyé dès 10 matchs ; les classements de saison ne sont pas concernés. Ajouter la
      constante aux « Fichiers clés ».
- [ ] **Étape 7** : vérifier : `./mvnw -B test` → `BUILD SUCCESS`, Docker lancé : `KickerMatchRepositoryTests`
      `Skipped: 0` ; `git diff origin/dev --stat -- src/main/java/com/leogouchon/hubscore/kicker_match_service/repository/KickerEloSeasonalRepository.java`
      vide, et `getSeasonStats` sans ligne modifiée dans `git diff origin/dev`.
- [ ] **Étape 8** : commit `feat(kicker): rank last week only active players and decouple last week elo`.
