# Workflow Git

Branches, commits et flux de merge. Le pas-à-pas d'une PR est dans
[../process/pull-request.md](../process/pull-request.md), celui d'une release dans
[../process/release.md](../process/release.md).

## Fichiers clés

- `.github/workflows/tests.yml` : tests sur chaque PR et push vers `dev` et `main`.
- `.github/workflows/release.yml` : semantic-release sur chaque push vers `main`.
- `.github/workflows/composeAndDeploy.yml` : image Docker et déploiement sur le VPS à chaque push vers `main`.
- `.releaserc` : configuration de semantic-release (version Maven, `CHANGELOG.md`, tag `v<version>`).
- `.github/pull_request_template.md` : modèle de description de PR.

## Branches

```
feature branch ──PR (squash)──► dev ──PR « release »──► main ──► release + déploiement
```

| Branche                      | Rôle             | Règle                                                                  |
| ---------------------------- | ---------------- | ---------------------------------------------------------------------- |
| `main`                       | Production       | Jamais de push direct. N'évolue que par la PR de release depuis `dev`  |
| `dev`                        | Intégration      | Base de toutes les branches de travail. Jamais de push direct          |
| `<type>/<kebab-description>` | Travail en cours | Créée depuis `dev` à jour, supprimée après le merge                    |

- **Nom de branche** : `<type>/<description-kebab>`, avec le même `type` que les commits.
    - Exemples : `feat/duo-stats`, `fix/access-code-creator`, `docs/project-documentation`.
- **Création** :
    ```bash
    git fetch origin
    git switch -c feat/ma-feature origin/dev
    ```
    Partir de `origin/dev` évite `git switch dev`, qui échoue quand `dev` est extrait dans un autre worktree.
- **Worktree Orca** : Orca nomme la branche `<utilisateur>/<nom>`. La renommer au format ci-dessus avant le premier
  commit (`git branch -m feat/ma-feature`), et vérifier qu'elle part de `dev`
  (`git merge-base --is-ancestor origin/dev HEAD`).
- **Une branche = un sujet.** Si la branche prend du retard, rebaser sur `dev` (`git rebase origin/dev`) plutôt que
  merger `dev` dedans.

## Commits : Conventional Commits

Format : [Conventional Commits 1.0.0](https://www.conventionalcommits.org/fr/v1.0.0/). semantic-release en déduit la
version : `feat` donne une version mineure, `fix` et `perf` un correctif, `!` une version majeure.

```
<type>(<scope>): <description>

[corps facultatif : le pourquoi, en anglais]
```

- **Description** : en **anglais**, à l'impératif, en minuscules, sans point final.
    - Exemple : `fix(kickerStats): return seasons stats even if no match has been played`.
- **Scope** : le domaine touché (`kickerMatches`, `kickerStats`, `auth`, `player`, `deploy`…). Il est facultatif si le
  changement est transverse.
- **Breaking change** (contrat d'API cassé pour kicker-app) : `!` après le scope (`feat(auth)!: …`), expliqué dans le
  corps.

| Type       | Usage dans ce projet                                                           |
| ---------- | ------------------------------------------------------------------------------ |
| `feat`     | Nouvelle fonctionnalité de l'API                                               |
| `fix`      | Correction de bug                                                              |
| `refactor` | Restructuration sans changement de comportement                                |
| `perf`     | Amélioration de performance                                                    |
| `docs`     | Documentation (`docs/`, `specs/`, `README.md`, `CLAUDE.md`)                    |
| `test`     | Ajout ou correction de tests                                                   |
| `chore`    | Maintenance : dépendances, configuration, environnement de dev                 |
| `ci`       | GitHub Actions, Docker, scripts de déploiement, Makefile                       |

- **Pas de `wip`** dans l'historique de `dev`. Les commits intermédiaires sont acceptés dans la branche, car le squash
  les fusionne.
- `chore(release): <version>` est réservé au commit créé par semantic-release.

## Merges

- **Branche → `dev`** : **squash merge**. Le titre de la PR devient le message du commit, au format Conventional
  Commits ; GitHub ajoute le numéro : `feat(auth): implement oidc auth service (#53)`.
- **`dev` → `main`** : PR de release, sans squash, pour que `main` reste un ancêtre de `dev`. Voir
  [../process/release.md](../process/release.md).
- Supprimer la branche après le merge.
