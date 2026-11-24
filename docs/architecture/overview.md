# Vue d'ensemble

La stack, le découpage en domaines et en couches, et le parcours d'une requête de bout en bout.

## Fichiers clés

- `src/main/java/com/leogouchon/hubscore/HubscoreApplication.java` : point d'entrée, `@EnableScheduling`, CORS MVC.
- `src/main/java/com/leogouchon/hubscore/common/` : configuration transverse et DTO communs.
- `pom.xml` : dépendances et plugins (Lombok, JaCoCo, Surefire).

## Stack

| Besoin            | Choix                                                                     |
| ----------------- | ------------------------------------------------------------------------- |
| Langage, runtime  | Java 21, Spring Boot 3.4                                                  |
| Web               | Spring Web (MVC, Tomcat), Jakarta Validation                              |
| Données           | Spring Data JPA (Hibernate), `JdbcTemplate`, PostgreSQL 17, Flyway        |
| Sécurité          | Spring Security, OAuth 2.0 resource server (JWT)                          |
| Documentation API | SpringDoc OpenAPI (annotations `@Operation`, `@ApiResponse`, `@Tag`)      |
| Boilerplate       | Lombok (`@Getter`, `@Setter`, `@Slf4j`)                                   |
| Tests             | JUnit 5, Mockito, AssertJ, MockMvc, H2 (mode PostgreSQL), JaCoCo          |
| Livraison         | Docker (image construite par Maven), semantic-release, VPS                |

## Domaines

Le code est découpé par domaine métier, chacun dans un package `<domaine>_service` :

| Package                | Contenu                                                                                          |
| ---------------------- | ------------------------------------------------------------------------------------------------ |
| `common`               | `config/` (sécurité, CORS, OpenAPI, gestion des erreurs), `dto/` (pagination, erreur)            |
| `user_service`         | utilisateurs (comptes), rôles, `/me`, invitations et provisioning venant de l'identity server     |
| `player_service`       | joueurs, teams (groupes de joueurs), informations kicker du joueur (ELO courant)                  |
| `kicker_match_service` | matchs, ELO global et saisonnier, statistiques, codes d'accès                                    |
| `kicker_common`        | outils de dev (`DevBootstrapRunner`)                                                             |

Les domaines ne sont pas étanches : `kicker_match_service` utilise `PlayerService` et les entités `Players` ;
`user_service` lit `PlayerRepository`.

## Couches

Dans chaque domaine :

```
controller/        @RestController : HTTP, validation (@Valid), droits (@PreAuthorize), DTO ↔ service
service/           interfaces XxxService : le contrat métier
service/impl/      XxxServiceDefault : la logique, les transactions (@Transactional)
repository/        interfaces Spring Data (requêtes dérivées, @Query JPQL ou natives)
repository/projection/   interfaces de projection pour les requêtes natives
specification/     Specifications JPA pour les filtres dynamiques
entity/            entités JPA
dto/               objets de requête et de réponse
exception/         exceptions métier
```

## Parcours d'une requête

Exemple : `POST /api/v1/kicker/matches`.

1. **Sécurité** (`SecurityConfig`) : CORS, puis validation du JWT (signature, émetteur, audience). Le convertisseur
   synchronise l'utilisateur (`IdentityUserSynchronizer`) et lui donne l'autorité `ROLE_<rôle>`.
2. **Controller** (`KickerMatchController.createMatch`) : désérialise et valide `KickerMatchRequestDTO`.
3. **Service** (`KickerMatchServiceDefault.createMatch`, transactionnel) : charge les joueurs, vérifie 1 contre 1 ou
   2 contre 2 et des joueurs distincts, enregistre le match, calcule l'ELO global puis saisonnier, et programme le
   rafraîchissement de la vue matérialisée après le commit.
4. **Réponse** : le controller relit le match en `KickerMatchResponseDTO` (ELO avant match, gains, chances de
   victoire) et renvoie `201` avec l'en-tête `Location`.

## Gestion des erreurs

`GlobalExceptionHandler` (`@RestControllerAdvice`) traduit les exceptions en `ApiErrorResponseDTO`
(`{ "message": "...", "errors": { "champ": "message" } }`) :

| Exception                                                                     | Statut                 |
| ----------------------------------------------------------------------------- | ---------------------- |
| `MethodArgumentNotValidException` (corps invalide)                            | 400, `errors` par champ |
| `InvalidKickerMatchAccessCodeException`, `InactiveKickerMatchAccessCodeException` | 401                 |
| `ResponseStatusException`                                                     | le statut porté        |
| `EntityNotFoundException`                                                     | 404                    |
| toute autre `RuntimeException`                                                | 400 avec son message   |

Ce dernier cas attrape aussi ce qui ne devrait pas l'être (voir [../tech-debt.md](../tech-debt.md)) :

- les erreurs inattendues (NPE, erreur SQL), qui sortent en `400` avec leur message ;
- le refus d'un `@PreAuthorize` (`AuthorizationDeniedException`, une `RuntimeException`), qui sort en `400` au lieu de
  `403` ;
- la validation des paramètres (`ConstraintViolationException`, due à `@Validated` sur les controllers).

Les refus émis par les filtres de Spring Security (jeton absent ou invalide : `401`) passent par
`response.sendError` et ne suivent pas ce format. La cible est dans
[../conventions/api-design.md](../conventions/api-design.md#erreurs).

## Tâches de fond

- `DevBootstrapRunner` (profil `dev`) : recalcule tout l'ELO au démarrage.
- `@EnableScheduling` est actif, mais aucune tâche `@Scheduled` n'existe aujourd'hui.
