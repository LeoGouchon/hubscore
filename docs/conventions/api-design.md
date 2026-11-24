# Conception de l'API

Comment écrire un endpoint : chemin, DTO, validation, statuts, erreurs, droits et documentation. La liste des
endpoints existants est dans [../architecture/api.md](../architecture/api.md).

## Fichiers clés

- `kicker_match_service/controller/KickerMatchController.java` : controller de référence (pagination, création,
  `Location`, droits).
- `common/config/GlobalExceptionHandler.java`, `common/dto/ApiErrorResponseDTO.java` : erreurs
  ([ADR 0006](../decisions/0006-format-des-erreurs.md)).
- `common/dto/PaginatedResponseDTO.java` : pagination.
- `common/config/SecurityConfig.java`, `common/config/OpenAPIConfig.java` : routes publiques, sécurité OpenAPI.

## Chemins

- Préfixe `/api/v1/`, ressource au pluriel en kebab-case : `/api/v1/kicker/matches`, `/api/v1/admin/kicker-match-codes`.
- Routes réservées à l'administration sous `/api/v1/admin/...` ; appels serveur à serveur sous `/api/internal/...`.
- Un identifiant de chemin est un UUID typé (`@PathVariable UUID id`).
- `PUT` remplace, `PATCH` modifie une partie de la ressource. Une action qui n'est pas du CRUD est un sous-chemin
  verbal en **`POST`** : `/recalculate-elo`, `/{id}/revoke`.

**État** : ⚠️ `PATCH /api/v1/admin/kicker-match-codes/{id}/revoke` (à garder tant que kicker-app l'appelle).

## DTO : des `record`

Les nouveaux DTO sont des `record` Java immuables, validés par Jakarta Validation :

```java
public record PlayerCreateRequestDTO(
        @NotBlank @Size(max = 100) String firstname,
        @NotBlank @Size(max = 100) String lastname,
        @NotEmpty List<UUID> teamIds
) {
}

public record PlayerSummaryResponseDTO(UUID id, String firstname, String lastname) {
    public static PlayerSummaryResponseDTO from(Players player) {
        return new PlayerSummaryResponseDTO(player.getId(), player.getFirstname(), player.getLastname());
    }
}
```

- **Jamais d'entité JPA** dans un corps de requête ou de réponse, ni dans un champ de DTO : on expose seulement ce que
  le client doit voir, et le schéma peut évoluer sans casser l'API.
- Le corps est validé par `@Valid @RequestBody`. Un objet imbriqué porte `@Valid` (`@Valid @NotNull MatchDTO match`),
  une liste d'objets `List<@Valid ItemDTO>` : sans cela, ses contraintes sont ignorées.
- Les contraintes sur les paramètres (`@Min`, `@Max` sur `page`, `size`) sont validées **nativement** par Spring MVC
  (Spring 6.1 et plus) : **pas de `@Validated` sur un controller**, qui basculerait vers une validation par proxy et une
  autre exception.
- Les paramètres de requête complexes ont leur classe dans `dto/controller_params/` (`PlayerFilterDTO`).

**État** : ⚠️ la plupart des DTO sont des classes Lombok ; entités exposées (`PlayerControllerV1`, `MeResponseDTO`,
`UserResponseDTO`, `UserController`) ; `@Validated` sur quatre controllers.

## Dates et enums

| Donnée                         | Type Java (DTO, service)  | JSON                                              |
| ------------------------------ | ------------------------- | ------------------------------------------------- |
| instant (création, expiration) | `Instant`                 | chaîne ISO 8601 en UTC : `"2026-11-24T09:30:00Z"` |
| jour sans heure                | `LocalDate`               | `"2026-11-24"`                                    |
| saison                         | `int year`, `int quarter` | deux nombres (`quarter` de 1 à 4)                 |
| enum                           | `enum` Java               | le nom de la constante : `"ACTIVE"`               |

- C'est le comportement par défaut de Spring Boot (Jackson, `WRITE_DATES_AS_TIMESTAMPS` désactivé) : **pas de
  `@JsonFormat`** sur un nouveau champ, jamais `java.util.Date` ni `java.sql.Timestamp` dans un nouveau DTO.
- **Paramètres de requête** : un `Instant` se lit en ISO sans annotation ; un `LocalDate` porte
  `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)` (sinon le format dépend de la locale).
- **Fuseau métier** : un jour ou une saison se découpe dans le fuseau `Europe/Paris`, jamais dans celui de la JVM
  ([code-style.md](code-style.md#le-temps)). Un instant voyage en UTC ; le front l'affiche dans le fuseau local.
- **Enums** : renommer ou supprimer une constante exposée casse le contrat. Une constante ajoutée est signalée au front.
- Stockage : [persistence.md](persistence.md#entités).

**État** : ⚠️ formats mélangés : ISO pour les matchs et l'historique d'ELO, millisecondes pour les codes d'accès et le
filtre `date` des matchs. Changement coordonné avec kicker-app ([../tech-debt.md](../tech-debt.md)).

## Statuts HTTP

| Situation                                             | Statut                              |
| ----------------------------------------------------- | ----------------------------------- |
| lecture réussie                                       | `200` avec le corps                 |
| création                                              | `201` + en-tête `Location` + corps  |
| suppression, action sans contenu                      | `204`                               |
| corps, paramètre ou règle d'entrée invalide           | `400`                               |
| non authentifié                                       | `401`                               |
| authentifié sans le droit                             | `403`                               |
| ressource introuvable                                 | `404`                               |
| conflit avec l'état (doublon, modification concurrente) | `409`                             |
| erreur inattendue                                     | `500`, message générique            |

## Erreurs

Toute erreur renvoyée par l'API a le format `ApiErrorResponseDTO` (`{ "message", "errors" }`, voir l'ADR 0006). Le
controller ne construit pas de réponse d'erreur : le service lève une exception, `GlobalExceptionHandler` la traduit.

- **Exceptions nommées** dans `<domaine>/exception/`, traduites une à une :
    - `<Entité>NotFoundException` → `404` (pas `jakarta.persistence.EntityNotFoundException`, que Hibernate lève aussi
      pour une référence orpheline) ;
    - `<Entité>AlreadyExistsException` et autres conflits métier → `409` ;
    - `IllegalArgumentException` levée par un invariant d'entité → `400`.
- **Exceptions de Spring MVC** (JSON illisible, paramètre manquant ou mal typé, validation, méthode non permise) :
  `GlobalExceptionHandler` étend `ResponseEntityExceptionHandler` et surcharge ses méthodes (`handleExceptionInternal`,
  `handleMethodArgumentNotValid`, `handleHandlerMethodValidationException`) pour produire le format commun. On ne
  redéclare pas de `@ExceptionHandler` pour ces exceptions : Spring refuserait de démarrer (handlers ambigus).
- **Base** : `DataIntegrityViolationException` (contrainte `UNIQUE` en concurrence) et
  `ObjectOptimisticLockingFailureException` (`@Version`) → `409` avec un message générique.
- **Sécurité** : `AccessDeniedException` et `AuthenticationException` ne sont **jamais** traduites par le handler : un
  `@ExceptionHandler` dédié les relance, pour que Spring Security réponde `403` ou `401`.
- **Filet** : `@ExceptionHandler(Exception.class)` → `500`, message `"Unexpected error"`, log `ERROR` avec la trace.
  Jamais de `@ExceptionHandler(RuntimeException.class)` qui renvoie le message brut.
- Messages en anglais, sans détail interne (nom de table, SQL, trace, nom de classe).

**État** : ⚠️ toute `RuntimeException` sort en `400` avec son message : un refus de `@PreAuthorize` sort donc en `400`
au lieu de `403`, un doublon en `400` au lieu de `409`, une erreur SQL avec son texte. Les codes d'accès invalides
sortent en `401`. Les `401` et `403` de Spring Security n'ont pas le format commun.

## Pagination

Une liste qui peut grandir est paginée : paramètres `page` (défaut 0, `@Min(0)`) et `size` (défaut 10, `@Min(1)`,
`@Max(50)`), réponse `PaginatedResponseDTO` construite depuis la `Page` de Spring Data.

## Droits

- **Routes publiques** (`SecurityConfig`) : `GET /api/v1/ping`, **tout `GET` sous `/api/v1/kicker/**` et
  `/api/v1/players/**`**, et deux `POST` protégés autrement (code d'accès, secret de provisioning). Tout le reste est
  authentifié. Un nouveau `GET` sous ces préfixes est donc public sans rien faire : une lecture réservée porte
  `@PreAuthorize` et son test `401`.
- Rendre une route publique est une décision : on l'ajoute à `SecurityConfig` et à
  [../architecture/security.md](../architecture/security.md).
- **Rôle** : `@PreAuthorize("hasRole('ADMIN')")` (ou `hasAnyRole`) sur la méthode, ou sur la classe quand tout le
  controller est réservé.
- **Droit lié à la ressource** (« seul le créateur du match peut… ») : `@PreAuthorize` ne le couvre pas. Le service le
  vérifie à partir de l'utilisateur courant et lève une exception traduite en `403`, avec un test dédié.
- **Utilisateur courant** : `@AuthenticationPrincipal Jwt jwt`, puis `jwt.getSubject()` vers `Users`. Jamais
  `@AuthenticationPrincipal Users` (le principal est un `Jwt`).
- Le front masque des boutons, mais **le backend est l'unique garant** : chaque règle de droit du brief est vérifiée
  ici et testée ([testing.md](testing.md)).

**État** : ⚠️ `UserController` sans rôle ; `@AuthenticationPrincipal Users` dans `KickerMatchController` et
`KickerMatchAccessCodeController`.

## Compatibilité avec le front

kicker-app consomme cette API. Avant de retirer, renommer ou changer le type d'un champ, un statut ou un droit :
vérifier son usage dans kicker-app (`src/hooks/`, `src/types/`), préférer **ajouter** et marquer l'ancien
`@Schema(deprecated = true)` (comme `deltaElo`), et noter la suppression à faire dans [../tech-debt.md](../tech-debt.md).

## Documentation OpenAPI

- Chaque endpoint a un `@Operation(summary = ...)` et un `@ApiResponse` par statut possible ; le controller porte un
  `@Tag`.
- La sécurité `bearerAuth` est déclarée **globalement** dans `OpenAPIConfig` : on ne répète pas
  `@SecurityRequirement` sur chaque route. Une route publique porte `@SecurityRequirements()` (vide).
- La liste de [../architecture/api.md](../architecture/api.md) est mise à jour dans la même PR.

**État** : ⚠️ `@SecurityRequirement` répété ; routes publiques affichées comme protégées ; springdoc 2.5 annoncé
incompatible avec Boot 3.4 ([../tech-debt.md](../tech-debt.md)).
