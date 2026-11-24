# Release

Livrer en production le contenu de `dev`, via une PR `dev` → `main`. Le merge dans `main` déclenche la version, le
changelog, l'image Docker et le déploiement.

## Fichiers clés

- `.github/workflows/release.yml` : `npx semantic-release` à chaque push sur `main` (sauf commit `chore(release)`).
- `.releaserc` : version calculée depuis les commits, `pom.xml` mis à jour (`mvn versions:set`), `CHANGELOG.md`,
  commit `chore(release): <version>`, tag `v<version>`, release GitHub.
- `.github/workflows/composeAndDeploy.yml` : image `hubscore:latest` poussée sur Docker Hub, puis sur le VPS
  `docker compose pull` et `up -d` du service `hubscore-backend`, et appel de `/api/v1/ping`.
- `Dockerfile` : build Maven (tests sautés) puis image `eclipse-temurin:21`.

> La configuration de production (`config/application.yml`, `.env`, `docker-compose.yml` du VPS) est sur le serveur,
> pas dans ce dépôt. Les secrets GitHub (`DOCKERHUB_*`, `VPS_*`, `RELEASE_TOKEN`) sont dans les réglages du dépôt.

## Préparer

- [ ] `dev` contient uniquement des PR mergées et vérifiées, CI verte.
- [ ] Sur `dev` à jour : `./mvnw -B test` passe.
- [ ] Test rapide sur `make dev` : `ping`, liste des matchs, création d'un match, statistiques, `GET /api/v1/me`.
- [ ] **Migrations** de la release relues : elles s'appliqueront sur les données de production au démarrage.
- [ ] **Configuration** : nouvelle propriété ajoutée au `config/application.yml` ou au `.env` du VPS **avant** le
      déploiement, sinon l'API ne démarre pas.
- [ ] **Ordre** : un changement requis côté identity server se déploie avant ; un front qui dépend d'un nouvel
      endpoint se déploie après ; un contrat cassé est coordonné avec la release de kicker-app.

## Ouvrir la PR

- **Base et source** : base `main`, source `dev`.
- **Titre** : `chore(release): <AAAA-MM-JJ>` (la version, elle, est calculée par semantic-release).
- **Description** : la liste des PR incluses depuis la dernière release, regroupées par type (`feat`, `fix`, autres),
  avec les migrations et les changements de configuration.

```bash
gh pr create --base main --head dev --title "chore(release): 2026-11-24"
```

## Merger

- **Sans squash** (« Rebase and merge » ou merge commit) : `main` doit rester un ancêtre de `dev`, et les messages
  Conventional Commits de chaque PR doivent arriver sur `main`, car semantic-release les lit pour calculer la version.
- Ne jamais committer directement sur `main`. Un correctif urgent passe par une branche `fix/…` depuis `dev`, puis par
  une release.

## Après le merge

- [ ] Les workflows `Release new version` et `CI/CD Backend Docker Deploy` sont verts ; le ping final répond.
- [ ] Le commit `chore(release): <version>` créé sur `main` est ramené dans `dev` (PR `main` → `dev`), pour que
      `pom.xml` et `CHANGELOG.md` restent alignés.
- [ ] Vérifier la production : une lecture, une écriture, une route authentifiée depuis kicker-app.
- [ ] En cas de régression : revert de la PR fautive sur `dev`, puis nouvelle release. Une migration déjà appliquée ne
      se défait pas par un revert : écrire une migration corrective.
