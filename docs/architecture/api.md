# API

Les endpoints exposés aujourd'hui, avec leurs droits, et les formats communs (pagination, erreurs). Le contrat
détaillé de chaque corps se lit dans les DTO et dans OpenAPI.

## Fichiers clés

- `*/controller/*.java` : les endpoints.
- `common/dto/PaginatedResponseDTO.java`, `common/dto/ApiErrorResponseDTO.java` : formats communs.
- `common/config/OpenAPIConfig.java` : description OpenAPI (schéma `bearerAuth`).
- `common/config/SecurityConfig.java` : routes publiques (voir [security.md](security.md)).

## Endpoints

Accès : **public**, **auth** (tout utilisateur connecté), ou rôle exigé.

### Santé et compte (`user_service`)

| Méthode et chemin                     | Accès          | Rôle                                                       |
| ------------------------------------- | -------------- | ---------------------------------------------------------- |
| `GET /api/v1/ping`                    | public         | renvoie `pong`                                             |
| `GET /api/v1/me`                      | auth           | utilisateur courant : id, e-mail, joueur lié, rôle         |
| `GET /api/v1/users?page&size`         | auth           | liste paginée des utilisateurs                             |
| `GET /api/v1/users/{id}`              | auth           | un utilisateur                                             |
| `POST /api/v1/users`                  | auth           | crée un utilisateur (rôle forcé à `USER`)                  |
| `PUT /api/v1/users/{id}`              | auth           | modifie e-mail et joueur lié                               |
| `DELETE /api/v1/users/{id}`           | auth           | supprime un utilisateur                                    |
| `PUT /api/v1/users/{id}/role`         | `ADMIN`        | change le rôle (`{ "role": "MODERATOR" }`)                 |
| `POST /api/v1/admin/invitation`       | `ADMIN`, `MODERATOR` | invitation pour un joueur sans compte (`{ playerId }`) |
| `POST /api/internal/identity-users`   | secret partagé | provisioning par l'identity server                         |

Les routes `/api/v1/users` sans rôle sont un écart connu (voir [../tech-debt.md](../tech-debt.md)).

### Joueurs et teams (`player_service`)

| Méthode et chemin                                  | Accès  | Rôle                                                  |
| -------------------------------------------------- | ------ | ----------------------------------------------------- |
| `GET /api/v1/players?page&size&sport&teamId`       | public | liste paginée (`size` 1 à 50)                         |
| `GET /api/v1/players/{id}`                         | public | un joueur                                             |
| `GET /api/v1/players/unlinked?sport&teamId`        | public | joueurs sans compte (pour les invitations)            |
| `POST /api/v1/players`                             | auth   | crée un joueur (`firstname`, `lastname`, `teamIds`)   |
| `DELETE /api/v1/players/{id}`                      | auth   | supprime un joueur                                    |
| `GET /api/v1/teams?isKicker&isSquash`              | auth   | teams filtrées par flags (par défaut : kicker)        |

### Matchs et statistiques (`kicker_match_service`)

| Méthode et chemin                                    | Accès   | Rôle                                                  |
| ---------------------------------------------------- | ------- | ----------------------------------------------------- |
| `GET /api/v1/kicker/matches`                         | public  | liste paginée et filtrée (voir plus bas)              |
| `GET /api/v1/kicker/matches/{id}`                    | public  | un match                                              |
| `POST /api/v1/kicker/matches`                        | auth    | crée un match                                         |
| `POST /api/v1/kicker/matches/public`                 | public  | crée un match avec un code d'accès (`{ code, match }`) |
| `DELETE /api/v1/kicker/matches/{id}`                 | `ADMIN` | supprime un match et recalcule l'ELO suivant          |
| `POST /api/v1/kicker/matches/recalculate-elo`        | `ADMIN` | recalcule tout l'ELO                                  |
| `GET /api/v1/kicker/stats/...`                       | public  | statistiques (voir [elo-and-stats.md](elo-and-stats.md)) |
| `POST /api/v1/admin/kicker-match-codes`              | `ADMIN` | crée un code d'accès                                  |
| `GET /api/v1/admin/kicker-match-codes`               | `ADMIN` | liste les codes (révoque au passage les expirés)      |
| `PATCH /api/v1/admin/kicker-match-codes/{id}/revoke` | `ADMIN` | révoque un code                                       |

**Filtres de `GET /api/v1/kicker/matches`** :

- `page` (défaut 0), `size` (1 à 50, défaut 10) ;
- `playerIds` : matchs où joue au moins un de ces joueurs ;
- `filter.operator` (`AND`, `OR`) et `filter.groups[i].operator` (`AND`, `OR`, `WITH`, `AGAINST`) avec
  `filter.groups[i].playerIds[j]` : filtre avancé ; `WITH` et `AGAINST` prennent exactement deux joueurs distincts ;
- `date` : un jour (timestamp en millisecondes), interprété dans le fuseau `Europe/Paris` ;
- `dateOrder` : `ascend` (défaut) ou `descend`.

**Match en réponse** (`KickerMatchResponseDTO`) : joueurs (avec ELO global et saisonnier avant match s'ils sont
visibles), scores, `eloWinTeamA` / `eloWinTeamB` (variation signée de chaque équipe), `winChanceTeamA` /
`winChanceTeamB`, `createdAt` (chaîne ISO 8601). Les champs `deltaElo` et `deltaEloSeasonal` sont dépréciés.

**Dates** : les matchs et l'historique d'ELO renvoient des chaînes ISO 8601 ; les codes d'accès échangent des
millisecondes (`createdAt`, `expiresAt`, `lastUsedAt`, `revokedAt`), comme le filtre `date` des matchs. Cible :
[../conventions/api-design.md](../conventions/api-design.md#dates-et-enums).

## Pagination

Les listes paginées renvoient `PaginatedResponseDTO` :

```json
{ "content": [], "currentPage": 0, "totalPages": 3, "totalElements": 25, "pageSize": 10 }
```

## Erreurs

Format `ApiErrorResponseDTO` : `{ "message": "...", "errors": { "champ": "message" } }`. Correspondance avec les
exceptions : [overview.md](overview.md#gestion-des-erreurs). Les `401` et `403` de Spring Security ne suivent pas ce
format.

## OpenAPI

SpringDoc génère la description à partir des annotations (`/v3/api-docs`, interface `/swagger-ui/index.html`). Ces
routes ne sont pas dans les routes publiques : elles demandent un jeton. Le titre affiché est encore « SquashApp API ».
