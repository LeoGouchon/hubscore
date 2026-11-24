# Hubscore

API REST de suivi des matchs de baby-foot : enregistrement des matchs, ELO global et saisonnier, classements et
statistiques. Spring Boot 3 (Java 21), PostgreSQL et Flyway ; l'authentification est déléguée à l'identity server
(resource server JWT). Le front est [kicker-app](https://github.com/LeoGouchon/kicker-app).

## Démarrage rapide

```bash
make install           # worktree Orca : .env et config/application.yml, puis build des images
make dev               # PostgreSQL + API dans Docker : http://localhost:8080/api/v1/ping
./mvnw -B test         # tests (H2 en mémoire, sans Docker)
```

L'identity server se lance séparément, selon son README (port `8081`).

| Commande         | Effet                                                                  |
| ---------------- | ---------------------------------------------------------------------- |
| `make dev`       | démarre `db` et `backend` ; ports 5432 et 8080 partagés entre worktrees |
| `make logs`      | logs de l'API                                                          |
| `make test`      | tests dans Docker et résumé de couverture JaCoCo                       |
| `make clean`     | supprime conteneurs, volumes et images du worktree                     |

## Documentation

Toute la documentation est dans [`docs/`](docs/README.md) :

- [Démarrage et configuration](docs/getting-started.md)
- [Architecture](docs/architecture/README.md) : domaines et couches, sécurité, persistance, ELO, endpoints
- [Conventions](docs/conventions/README.md) : style Java, structure, API, persistance, tests, Git
- [Process](docs/process/README.md) : développer une feature, ajouter un endpoint ou une migration, PR, release
- [Décisions (ADR)](docs/decisions/README.md) et [dette technique](docs/tech-debt.md)

Les agents IA (Claude Code) démarrent par [`CLAUDE.md`](CLAUDE.md).
