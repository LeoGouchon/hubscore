# Démarrage

Installer le projet, le configurer et le lancer en local, avec ou sans Docker.

## Fichiers clés

- `config/application.example.yml` : modèle de la configuration Spring, copié en `config/application.yml` (ignoré
  par Git). Il n'y a **aucun** `application.yml` dans `src/main/resources`.
- `.env` : variables lues par Docker Compose et passées au conteneur de l'API (ignoré par Git). Spring ne le lit pas
  directement.
- `docker-compose.yml` : services `db` (PostgreSQL 17), `backend` (l'API) et `tests` (Maven).
- `Makefile` et `scripts/worktree-install.ps1` : cibles `install`, `dev`, `test`, `clean`…
- `src/test/resources/application-test.yml` : configuration des tests (H2 en mémoire, Flyway désactivé).

## Prérequis

- **Docker** pour lancer l'API et PostgreSQL (`make dev`, `make test`).
- **Java 21** pour lancer les tests sans Docker (`./mvnw -B test`). Maven n'est pas requis : le wrapper `mvnw` le
  télécharge.
- **PowerShell** (Windows) : les cibles `make install`, `make config` et `make test` l'appellent.
- L'**identity server** démarré à part (dépôt `identity-server`), sur `http://localhost:8081`, pour tout appel
  authentifié.

## Installation

```bash
make install    # worktree : copie .env et config/application.yml du checkout principal (ou depuis l'exemple),
                # puis construit les images sans les démarrer
make dev        # crée config/application.yml si absent, puis démarre db + backend
```

Chaque worktree a son propre projet Docker Compose (`hubscore-<worktree>`) ; le checkout principal garde `hubscore`.
Les ports **5432** et **8080** sont partagés : un seul checkout peut faire tourner `make dev` à la fois.

Au démarrage, Flyway applique les migrations de `src/main/resources/db/migration`. Avec le profil `dev` (imposé par
`docker-compose.yml`), `DevBootstrapRunner` recalcule tout l'ELO.

**Données de dev** : `src/main/resources/db/dev-data/V9999__seed_dev_data.sql` crée des joueurs et des matchs fictifs.
Il n'est chargé que si `FLYWAY_LOCATIONS` vaut `classpath:db/migration,classpath:db/dev-data`. Numéroté `V9999`, il
passe devant toute nouvelle migration : après un `git pull` qui en apporte une, repartir d'une base vide
(`make rebuild`). Le passage en migration répétable est dans [tech-debt.md](tech-debt.md).

## Configuration

`config/application.yml` lit des variables d'environnement, avec des valeurs par défaut pour le local :

| Variable                                                | Rôle                                                          | Défaut local                             |
| ------------------------------------------------------- | ------------------------------------------------------------- | ---------------------------------------- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`                  | connexion PostgreSQL                                          | `jdbc:postgresql://db:5432/hubscore`…    |
| `DB_NAME`, `DB_USER`                                    | base et utilisateur créés par le conteneur `db`               | `hubscore`                               |
| `DB_POOL_MINIMUM_IDLE`, `DB_POOL_MAXIMUM_SIZE`          | pool Hikari                                                   | `1`, `5`                                 |
| `FLYWAY_ENABLED`, `FLYWAY_LOCATIONS`, `FLYWAY_DB_*`     | migrations (et données de dev, voir plus haut)                | activé, `classpath:db/migration`         |
| `PORT`                                                  | port HTTP                                                     | `8080`                                   |
| `IDENTITY_ISSUER`                                       | émetteur attendu dans les jetons                              | `http://localhost:8081`                  |
| `IDENTITY_INTERNAL_URL`                                 | URL de l'identity server vue depuis le conteneur              | `IDENTITY_ISSUER`                        |
| `IDENTITY_JWK_SET_URI`                                  | clés publiques de signature des jetons                        | `<issuer>/oauth2/jwks`                   |
| `IDENTITY_AUDIENCE`                                     | audience exigée dans les jetons                               | `default-api`                            |
| `BACKEND_PROVISIONING_SECRET`                           | secret partagé avec l'identity server (provisioning, invites) | `hubscore-secret`                        |
| `IDENTITY_CLIENT_ID`                                    | client hubscore déclaré dans l'identity server                | `hubscore-client`                        |
| `CORS_ALLOWED_ORIGINS`                                  | origines autorisées (liste)                                   | `http://localhost:4200,…:5173`           |

Une nouvelle propriété s'ajoute à `config/application.example.yml`, à `src/test/resources/application-test.yml` si
le contexte de test en a besoin, et à ce tableau. En production, `config/application.yml` et `.env` sont sur le
serveur, jamais dans le dépôt.

## Commandes

| Commande          | Effet                                                                                  |
| ----------------- | -------------------------------------------------------------------------------------- |
| `./mvnw -B test`  | toute la suite de tests sur H2, sans Docker (référence de la CI)                       |
| `make dev`        | démarre `db` et `backend` en arrière-plan ; API sur `http://localhost:8080`            |
| `make logs`       | suit les logs de l'API                                                                 |
| `make ps`         | état des conteneurs                                                                    |
| `make test`       | tests dans Docker, rapport JaCoCo et résumé de couverture                              |
| `make rebuild`    | **supprime les volumes** (base locale vidée), puis reconstruit et redémarre            |
| `make clean`      | supprime conteneurs, volumes et images locales du worktree                             |

Rapport de couverture : `target/site/jacoco/index.html`. Santé de l'API : `GET /api/v1/ping` → `pong`.

## Identity server

- Hubscore valide la signature, l'émetteur et l'audience des jetons ; le `sub` du jeton correspond à
  `users.identity_user_id`. Détail : [architecture/security.md](architecture/security.md).
- L'identity server doit lister `default-api` dans ses backends autorisés (ou `IDENTITY_AUDIENCE` doit prendre la
  valeur qu'il utilise), et connaître le même secret de provisioning.

## Problèmes fréquents

- **L'API ne démarre pas, `Schema-validation`** : une entité ne correspond pas au schéma (`ddl-auto: validate`). Il
  manque une migration.
- **`401` sur tous les appels authentifiés** : émetteur ou audience différents entre le jeton et la configuration,
  ou identity server injoignable depuis le conteneur (`IDENTITY_INTERNAL_URL`, `IDENTITY_JWK_SET_URI`).
- **Port 5432 ou 8080 déjà pris** : un autre checkout fait tourner `make dev`. L'arrêter (`docker compose down` dans
  ce checkout).
