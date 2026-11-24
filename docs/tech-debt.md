# Dette technique

Écarts connus entre le code et les [conventions](conventions/README.md), bugs repérés et code mort. Relevé du
2026-11-24, par lecture du code et revue indépendante (les points « à confirmer » n'ont pas encore été reproduits).

**Règles** :

- on ne recopie jamais un pattern listé ici ;
- une PR qui corrige un point le retire de la liste ;
- une PR qui découvre un écart sans le corriger l'ajoute ;
- les chemins sont relatifs à `src/main/java/com/leogouchon/hubscore/` ;
- les correctifs de code se font dans des PR `fix` ou `refactor` dédiées, pas dans une PR de doc.

**Priorités** : **P1** = bug ou risque (sécurité, données), **P2** = écart de convention qui se propage, **P3** =
nettoyage.

## P1 : bugs et risques

| Sujet | Où | Action |
| --- | --- | --- |
| `/api/v1/users` (liste, détail, création, modification, suppression) ouvert à **tout utilisateur connecté** ; corps et réponses en entité `Users` | `user_service/controller/UserController.java` | Réserver à `ADMIN` (ou supprimer ce que kicker-app n'utilise pas), DTO `record` |
| `DELETE /api/v1/players/{id}` (et `POST`) ouvert à tout utilisateur connecté | `player_service/controller/PlayerControllerV1.java` | Décider du rôle requis, `@PreAuthorize` |
| Le refus d'un `@PreAuthorize` (`AuthorizationDeniedException`) est attrapé par le handler `RuntimeException` : `400` au lieu de `403`. _À confirmer_ par un test | `common/config/GlobalExceptionHandler.java` | Relancer `AccessDeniedException` et `AuthenticationException` ([ADR 0006](decisions/0006-format-des-erreurs.md)) |
| Toute `RuntimeException` inattendue (NPE, erreur SQL) sort en `400` avec son message brut ; un doublon concurrent (`DataIntegrityViolationException`) aussi, avec le texte SQL | `common/config/GlobalExceptionHandler.java`, `KickerMatchAccessCodeServiceDefault.resolveCode` | `500` générique et log ; `409` pour les conflits ; exceptions nommées |
| `@AuthenticationPrincipal Users` reçoit `null` (le principal est un `Jwt`) : `created_by` des matchs jamais rempli, création de code d'accès probablement en échec (`created_by NOT NULL`, NPE). _À confirmer_ | `KickerMatchController.createMatch`, `KickerMatchAccessCodeController.createCode` | Résoudre l'utilisateur depuis `jwt.getSubject()` ; test MockMvc avec `jwt()` |
| Filtre `teamId` des joueurs : `root.get("teamId")` n'existe pas sur `Players`, la requête échoue (400). _À confirmer_ | `player_service/specification/PlayerSpecifications.byTeam` | Joindre `playerTeams.team.id` ; test `@DataJpaTest` |
| Recalcul complet de l'ELO non atomique : deux transactions `REQUIRES_NEW` sans transaction englobante, un échec du saisonnier laisse le global recalculé | `KickerMatchServiceDefault.recalculateElo`, `GlobalKickerEloService`, `SeasonalKickerEloService` | Une seule transaction, via la façade ELO (P2) |
| Saison d'un match calculée dans le fuseau de la JVM (UTC dans Docker) alors que les filtres de date utilisent `Europe/Paris` : un match du 1er avril à 0 h 30 compte pour le trimestre précédent. _À confirmer_ | `SeasonalKickerEloService.getSeason`, `KickerStatServiceDefault` (`Calendar`, `LocalDate.now()`) | Fuseau `Europe/Paris` explicite, `Clock` ; test aux frontières de trimestre |
| Secret de provisioning comparé avec `String.equals` (comparaison non constante) | `user_service/controller/IdentityProvisioningController.java` | `MessageDigest.isEqual` |
| Fiche ou duo d'un joueur introuvable : `RuntimeException` (400) ou `.get()` sur un `Optional` vide | `KickerStatServiceDefault.getPlayerStats`, `getDuoStats` | `PlayerNotFoundException` → `404` ; pas de `.get()` |

## P2 : migration vers les conventions

| Sujet | Où | Action |
| --- | --- | --- |
| Entités JPA exposées dans l'API | `PlayerControllerV1` (liste, détail, `unlinked`), `MeResponseDTO`, `UserResponseDTO`, `UserController` | DTO `record` de réponse |
| `@Setter` de classe sur toutes les entités (modèle anémique : `setStatus(ACTIVE)` contourne `revoke()`, `setPlayer2A(null)` casse 1 contre 1 / 2 contre 2) | `*/entity/` | Méthodes d'intention, setters retirés au fil des PR |
| ELO global et saisonnier appelés un par un (et le rafraîchissement de la vue) dans chaque écriture de match | `KickerMatchServiceDefault` | Façade `KickerEloRecalculationService` qui injecte `List<KickerEloService>` |
| `@Validated` sur les controllers : validation par proxy, `ConstraintViolationException` sortie en `400` brut, sans `errors` | `KickerMatchController`, `KickerStatsController`, `PlayerControllerV1`, `UserController` | Retirer ; traduire `HandlerMethodValidationException` |
| Erreurs de Spring MVC (JSON illisible, paramètre manquant ou mal typé, `405`) sous trois formats ; `EntityNotFoundException` et `ResponseStatusException` au lieu d'exceptions nommées | `GlobalExceptionHandler`, services et controllers | Étendre `ResponseEntityExceptionHandler` ([ADR 0006](decisions/0006-format-des-erreurs.md)) |
| `open-in-view` actif (défaut), 13 relations `@ManyToOne` / `@OneToOne` en `EAGER` (5 sur `KickerMatches`), aucune lecture en `readOnly` | `config/application.example.yml`, `*/entity/`, services | `open-in-view: false`, `LAZY`, `JOIN FETCH` ciblés, `readOnly = true` |
| Formats de date mélangés dans l'API : ISO (matchs, historique d'ELO) et millisecondes (codes d'accès, filtre `date`) ; `Date`, `Timestamp`, `Calendar` au lieu de `java.time` ; colonnes `TIMESTAMP` sans fuseau ; aucun `Clock` | `KickerMatchAccessCode*DTO`, `KickerMatchController.getMatches`, `KickerMatchResponseDTO`, `EloHistoryDTO`, migrations | ISO et `Instant`, en coordination avec kicker-app (`KickerMatchAccessCode.type.ts`) |
| DTO en classes Lombok (`@Getter @Setter`) | la plupart des `dto/` | `record` quand le DTO est touché |
| Requêtes natives mappées directement sur des DTO de l'API | `KickerMatchRepository`, `KickerEloRepository`, `KickerEloSeasonalRepository` | Projections dans `repository/projection/` |
| Controllers qui appellent un repository | `MeController`, `RoleController`, `IdentityProvisioningController` | Passer par un service |
| Services sans interface ; HTTP dans un service (`ResponseStatusException`) | `InvitationService`, `IdentityUserSynchronizer` | Interface + `XxxServiceDefault`, exceptions nommées |
| Injection : `@Autowired` redondant, `@Value` sur des champs ; `identity.*` lu par `@Value` dans trois classes ; pas de `@ConfigurationPropertiesScan` | `SecurityConfig`, `InvitationService`, `IdentityProvisioningController`, `HubscoreApplication` | `record IdentityProperties` validé, scan des propriétés |
| Bean instancié avec `new` ou injecté par sa classe concrète | `SeasonalKickerEloService` (`new EloCalculatorDefault()`), `EloMatrixServiceDefault` | Injecter `EloCalculatorService` |
| `try/catch` qui renvoie une `RuntimeException` générique ; `throws RuntimeException` | `UserServiceDefault.deleteUser`, `PlayerServiceDefault.createPlayer`, `MeController` | Exceptions nommées, pas d'enveloppe |
| `jakarta.transaction.Transactional` au lieu de celui de Spring | `PlayerServiceDefault` | `org.springframework.transaction.annotation.Transactional` |
| Seed de dev en migration versionnée `V9999` : toute nouvelle migration est ignorée sur une base locale qui l'a chargé | `src/main/resources/db/dev-data/V9999__seed_dev_data.sql` | `R__seed_dev_data.sql` idempotent (ou callback `afterMigrate`) |
| springdoc 2.5.0 annoncé incompatible avec Spring Boot 3.4 (`/v3/api-docs` en erreur dès qu'un `@RestControllerAdvice` existe). _À confirmer_ | `pom.xml` | springdoc 2.7 ou 2.8 |
| Requêtes dans des boucles (N+1) | `KickerStatServiceDefault` (`getLastFiveResultsByPlayerId` par joueur, `getPlayer` par duo) | Une requête groupée |
| Code dupliqué entre classement global et saisonnier, et entre ELO global et saisonnier | `KickerStatServiceDefault`, `GlobalKickerEloService`, `SeasonalKickerEloService` | Factoriser le calcul commun |
| Annotations Jackson sur des entités ; constructeurs publics sans argument | `Players`, `PlayerTeam`, `KickerMatches`, `Teams`… | À retirer une fois les entités plus exposées |
| Tests : un seul test de controller, filtres désactivés, aucun test de droit ; `spring-security-test` absent ; `@TestPropertySource` au lieu du profil `test` | `src/test/`, `pom.xml` | Voir `conventions/testing.md` |
| Champs dépréciés `deltaElo`, `deltaEloSeasonal` dans les matchs | `KickerMatchResponseDTO` | Retirer quand kicker-app ne les lit plus |

## P3 : nettoyage et nommage

| Sujet | Où | Action |
| --- | --- | --- |
| **Nommage non standard Java, conservé volontairement** ([ADR 0004](decisions/0004-organisation-par-domaine.md)) : packages en snake_case (`kicker_match_service`, `controller_params`), entités au pluriel (`Players`, `KickerMatches`) | tout `src/` | Ne rien faire sans nouvel ADR ; si renommage, PR dédiée |
| Restes du squash : flags `isSquash`, paramètre `sport=squash`, fichier `MatchesControllerTests.java~` suivi par Git | `Teams`, `TeamResponseDTO`, `PlayerSpecifications`, `src/test/java/com/leogouchon/squashapp/` | Supprimer (avec migration pour `is_squash`) |
| Action non CRUD en `PATCH` | `KickerMatchAccessCodeController` (`/{id}/revoke`) | `POST`, en coordination avec kicker-app |
| Nommage des tests : `KickerMatchSpecificationsTest`, classe `EloCalculatorDefaultTest` dans `EloCalculatorDefaultTests.java` | `src/test/` | `<Classe>Tests` |
| CORS déclaré deux fois | `HubscoreApplication.addCorsMappings`, `SecurityConfig` | Garder la configuration de Spring Security |
| OpenAPI : titre « SquashApp API », `@SecurityRequirement` répété par route, routes publiques affichées protégées, Swagger UI derrière l'authentification | `common/config/OpenAPIConfig.java`, controllers, `SecurityConfig` | Renommer ; `@SecurityRequirements()` sur les routes publiques ; doc ouverte en dev seulement |
| `401` / `403` des filtres de Spring Security hors du format `ApiErrorResponseDTO` | `SecurityConfig` (`sendError`) | `AuthenticationEntryPoint` et `AccessDeniedHandler` au format commun |
| Propriété `cookie.secure` sans usage ; `@EnableScheduling` sans tâche | `config/application.example.yml`, `HubscoreApplication` | Supprimer |
| `PlayerResponseDTO` échappe les noms avec un utilitaire de Tomcat dans un seul constructeur | `player_service/dto/PlayerResponseDTO.java` | Supprimer l'échappement (rôle du client) |
| `KickerMatchRequestDTO` : getters inutiles, exemple de score `11` et mention de `pointHistory` obsolètes | `kicker_match_service/dto/KickerMatchRequestDTO.java` | `record` validé (score à 10) |
| `package.json` avec description et script `test` factices (sert seulement à semantic-release) | `package.json` | Nettoyer |

## Outillage

| Sujet | Action |
| --- | --- |
| Tests sur H2 : la plupart des requêtes natives PostgreSQL et la vue matérialisée ne sont pas testées | Infrastructure Testcontainers en place ([ADR 0007](decisions/0007-tests-postgresql-testcontainers.md)) : couvrir les autres requêtes natives et la vue |
| `testcontainers.version` fixée à la main dans `pom.xml` (Spring Boot 3.4.2 gère 1.20.x, incompatible avec Docker Engine 29+) | Retirer la propriété à la montée de Spring Boot |
| Aucun formateur ni analyse statique | Spotless (ou Checkstyle) en CI |
| La CI ne construit pas l'image Docker sur les PR | Ajouter un `docker build` au workflow de tests |
| Limite de 200 lignes contrôlée uniquement pour les écritures de Claude Code | Le hook vit dans le plugin `gouchon` : ajouter un contrôle en CI pour `docs/**/*.md` |
| Vérification par rôle manuelle faute de jeton de test en local | Documenter comment obtenir un jeton de test depuis l'identity server de dev |
