# Sécurité

Hubscore est un resource server OAuth 2.0 : il ne gère ni mot de passe ni session, il valide les jetons JWT émis par
l'identity server et en déduit l'utilisateur et son rôle.

## Fichiers clés

- `common/config/SecurityConfig.java` : décodeur JWT, conversion en rôle, routes publiques, CORS, réponses 401/403.
- `user_service/service/IdentityUserSynchronizer.java` : met à jour l'utilisateur à partir des claims du jeton.
- `user_service/controller/IdentityProvisioningController.java` : création d'utilisateur appelée par l'identity server.
- `user_service/service/InvitationService.java` : création d'invitation déléguée à l'identity server.
- `user_service/entity/Users.java`, `UserRole.java` : compte et rôle.

## Validation du jeton

`jwtDecoder` construit un `NimbusJwtDecoder` sur `identity.jwk-set-uri` et vérifie :

- la signature (clés publiées par l'identity server) ;
- l'émetteur (`identity.issuer`) et les dates de validité ;
- l'audience : le claim `aud` doit contenir `identity.audience` (`default-api` en local).

La session est **sans état** (`STATELESS`), CSRF est désactivé. Une requête sans jeton valide sur une route protégée
reçoit `401 Unauthorized`. Un rôle insuffisant devrait recevoir `403 Forbidden`, mais le refus d'un
`@PreAuthorize` est aujourd'hui attrapé par `GlobalExceptionHandler` et sort en `400` (voir
[overview.md](overview.md#gestion-des-erreurs) et [../tech-debt.md](../tech-debt.md)).

## Du jeton au rôle

À chaque requête authentifiée, le convertisseur :

1. appelle `IdentityUserSynchronizer.synchronize(jwt)` : si un utilisateur a `identity_user_id = sub`, son e-mail et
   le prénom / nom de son joueur sont mis à jour depuis les claims (`email`, `given_name` / `firstName`,
   `family_name` / `lastName`) ;
2. cherche l'utilisateur par `sub` et donne l'autorité `ROLE_<role>` (`USER`, `MODERATOR` ou `ADMIN`) ;
3. à défaut d'utilisateur, donne `ROLE_USER`.

Le principal de la requête reste le `Jwt`. Un controller qui a besoin de l'utilisateur le retrouve par
`jwt.getSubject()` (comme `MeController`). `@AuthenticationPrincipal Users` ne fonctionne pas avec ce principal (voir
[../tech-debt.md](../tech-debt.md)).

## Rôles et routes

Règles de `SecurityConfig`, dans l'ordre :

| Route                                        | Accès                                    |
| -------------------------------------------- | ---------------------------------------- |
| `OPTIONS /**`                                | public (pré-requêtes CORS)               |
| `GET /api/v1/ping`                           | public                                   |
| `POST /api/v1/kicker/matches/public`         | public (protégé par un code d'accès)     |
| `POST /api/internal/identity-users`          | public (protégé par un secret partagé)   |
| `GET /api/v1/kicker/**`, `GET /api/v1/players/**` | public                              |
| tout le reste                                | authentifié                              |

Les restrictions par rôle sont posées sur les méthodes avec `@PreAuthorize` (`@EnableMethodSecurity`) :

- `ADMIN` : codes d'accès, suppression de match, recalcul de l'ELO, changement de rôle ;
- `ADMIN` ou `MODERATOR` : création d'invitation.

Le détail par endpoint est dans [api.md](api.md).

## Provisioning et invitations

- **Provisioning** : quand un compte est créé côté identity server, celui-ci appelle
  `POST /api/internal/identity-users` avec l'en-tête `X-Identity-Provisioning-Secret`. Hubscore crée ou met à jour
  l'utilisateur (`identity_user_id`, e-mail) et, si un `playerId` est fourni, le relie à ce joueur.
- **Invitation** : `POST /api/v1/admin/invitation` (`{ playerId }`) refuse un joueur déjà lié à un compte (`409`),
  puis appelle `POST <identity.internal-url>/api/internal/invitations` avec le même secret et le client
  `identity.identity-client-id`. La réponse (jeton, URL d'inscription) est renvoyée telle quelle au front.

Le secret est partagé avec l'identity server ; chaque backend peut avoir le sien.

## CORS

Les origines autorisées viennent de `hubscore.cors.allowed-origins` (`CorsProperties`). Elles sont déclarées deux
fois : dans `SecurityConfig` (`CorsConfigurationSource`, utilisé par Spring Security) et dans
`HubscoreApplication.addCorsMappings` (MVC).
