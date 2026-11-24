# 0002. API en resource server, identité déléguée à l'identity server

- **Statut** : Accepté
- **Date** : 2026-08-21 (rédigé le 2026-11-24, d'après la PR #53)

## Contexte

Hubscore gérait lui-même ses comptes : mots de passe, jetons JWT signés par un secret, refresh tokens et invitations
(tables `refresh_token`, `invitation_token`). Un identity server commun a été créé pour plusieurs applications : il
porte les comptes, la connexion (OIDC, Authorization Code + PKCE côté front) et les invitations.

## Options envisagées

1. **Garder l'authentification locale** : aucun service de plus, mais chaque application refait comptes, mots de
   passe et sécurité.
2. **Resource server OAuth 2.0** : hubscore ne fait que valider les jetons de l'identity server.

## Décision

Nous retenons l'option 2 :

- les jetons sont validés par signature (JWKS), émetteur et audience (`identity.*`) ;
- `users.identity_user_id` relie le `sub` du jeton à l'utilisateur hubscore ;
- **les rôles restent dans hubscore** (`users.role` : `USER`, `MODERATOR`, `ADMIN`), car ils sont propres à
  l'application ; ils sont lus en base à chaque requête ;
- l'identity server provisionne les utilisateurs (`POST /api/internal/identity-users`) et crée les invitations, avec
  un secret partagé ;
- les tables d'authentification locale sont supprimées (V19).

## Conséquences

- **Positives** : plus de mot de passe ni de secret de signature dans hubscore ; même compte pour toutes les
  applications.
- **Négatives / coûts** :
    - pour tout appel authentifié en local, l'identity server doit tourner ;
    - chaque requête authentifiée lit (et peut écrire) l'utilisateur en base pour synchroniser e-mail et nom ;
    - le principal Spring est le `Jwt`, pas l'utilisateur : le code doit le résoudre lui-même.
- **Suivi** : voir [../architecture/security.md](../architecture/security.md) ; les écarts (`@AuthenticationPrincipal
  Users`, routes `/users` sans rôle) sont dans [../tech-debt.md](../tech-debt.md).
