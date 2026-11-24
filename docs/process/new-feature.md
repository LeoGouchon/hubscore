# Développer une nouvelle feature

Le parcours complet, du besoin au merge dans `dev`. Chaque étape renvoie vers la doc qui détaille les règles.
Coche mentalement chaque case : une case sautée finit en dette.

## Avec Claude Code : `/gouchon:feature`

La skill `/gouchon:feature` (plugin `claude-workflow`, profil du projet dans `.claude/workflow.md`) déroule ce
parcours en quatre phases. Chacune produit un fichier dans `specs/<feature>/`, que tu valides avant la phase
suivante :

1. **Cadrer** (`/gouchon:cadrer`) : tours de questions jusqu'à ce que le besoin soit tranché (contrat d'API, droits,
   données) ; glossaire et ADR mis à jour au passage → `1-brief.md`.
2. **Planifier** (`/gouchon:planifier`) : tâches vérifiables tirées du brief, chacune avec ses tests → `2-plan.md`.
3. **Implémenter** (`/gouchon:implementer`) : exécution tâche par tâche, changement minimal, `./mvnw -B test` à
   chaque tâche → `3-execution.md`.
4. **Vérifier** (`/gouchon:verifier`) : tests, critères d'acceptation, revue indépendante, appels réels sur
   `make dev`, dette reportée → `4-verification.md`, avec la checklist des rôles à faire toi-même.

**Une session par phase** : quand une phase est validée et commitée, ferme la session (ou `/clear`) et relance
`/gouchon:feature` dans une nouvelle. Relancer `/gouchon:feature` reprend à la première phase non validée, y compris
depuis une autre machine après un `git push`. Fonctionnement détaillé : [specs/README.md](../../specs/README.md).
Pourquoi : [ADR 0005](../decisions/0005-workflow-feature.md).

Les sections suivantes restent la checklist de référence, avec ou sans l'agent.

## 1. Créer la branche

La branche vient en premier : le cadrage y commite déjà le brief, le glossaire et les ADR.

```bash
git fetch origin
git switch -c feat/<description-kebab> origin/dev
```

Dans un worktree Orca, renommer la branche `<utilisateur>/<nom>` avant de commencer
(`git branch -m feat/<description-kebab>`). Convention : [../conventions/git-workflow.md](../conventions/git-workflow.md).

## 2. Cadrer

- [ ] Reformuler le besoin en une ou deux phrases : quel appel, pour quel rôle (anonyme, user, modérateur, admin).
- [ ] Écrire le contrat : chemin, méthode, paramètres, corps, réponse, codes d'erreur. Vérifier ce que kicker-app
      attend (ou attendra) ; un changement de contrat existant est une rupture à coordonner avec le front.
- [ ] Données : nouvelle table ou colonne, migration des données existantes, effet sur l'ELO et les statistiques.
- [ ] Repérer l'existant réutilisable : services du domaine, `common/`, fabriques de `testutil/`.
- [ ] Une décision structurante (nouvelle librairie, nouveau pattern, changement d'architecture) ? → ADR dans
      [../decisions/](../decisions/README.md) **avant** de coder.
- [ ] Employer les termes de [../glossary.md](../glossary.md). Un nouveau terme métier y est ajouté.

## 3. Implémenter, dans cet ordre

1. **Migration** Flyway si le schéma change : [add-a-migration.md](add-a-migration.md).
2. **Entité et repository** : [../conventions/persistence.md](../conventions/persistence.md).
3. **Service** : interface `XxxService`, implémentation `XxxServiceDefault`, transactions, exceptions métier ; **avec
   son test unitaire**.
4. **DTO** `record` de requête et de réponse, validés.
5. **Controller** : route, `@PreAuthorize` ou route publique déclarée, statuts, OpenAPI ; **avec son test MockMvc**.
   Voir [add-an-endpoint.md](add-an-endpoint.md).
6. **Données dérivées** : après une écriture de match, ELO recalculé et vue matérialisée rafraîchie.

Pendant l'implémentation, respecter [code-style.md](../conventions/code-style.md),
[project-structure.md](../conventions/project-structure.md), [api-design.md](../conventions/api-design.md) et
[testing.md](../conventions/testing.md).

## 4. Vérifier

- [ ] `./mvnw -B test` passe (0 échec, 0 erreur).
- [ ] Test manuel sur `make dev` :
    - [ ] l'API démarre (migrations appliquées, `ddl-auto: validate` sans erreur) ;
    - [ ] chaque endpoint touché répond comme le brief (cas nominal et erreurs) ;
    - [ ] chaque rôle concerné : `401` sans jeton, `403` sans le rôle, succès avec ;
    - [ ] requête native nouvelle ou modifiée vérifiée sur PostgreSQL (non couverte par les tests H2).
- [ ] Aucune entité exposée, aucun controller qui appelle un repository, aucun `System.out` ni code commenté.

## 5. Documenter

- [ ] Endpoint ajouté ou modifié → [../architecture/api.md](../architecture/api.md).
- [ ] Route publique ou rôle changé → [../architecture/security.md](../architecture/security.md).
- [ ] Nouvelle convention ou convention modifiée → `docs/conventions/`.
- [ ] Nouveau mécanisme transverse → `docs/architecture/`.
- [ ] Nouvelle propriété de configuration → `config/application.example.yml`, `application-test.yml` si besoin,
      [../getting-started.md](../getting-started.md).
- [ ] Nouveau terme métier ou sens précisé → [../glossary.md](../glossary.md).
- [ ] Dette repérée mais non traitée (dont les commentaires `shortcut:`) → [../tech-debt.md](../tech-debt.md) ; dette
      traitée au passage → la retirer.
- [ ] Chaque fichier touché reste sous 200 lignes (ou l'exception est assumée dans `.claude/doc-length-ignore.txt`),
      et chaque nouveau fichier est ajouté au sommaire de son dossier.

## 6. Proposer

- [ ] Commits au format Conventional Commits.
- [ ] PR vers `dev` avec le template rempli. Suivre [pull-request.md](pull-request.md).
- [ ] Avec Claude Code : faire relire la branche avant de demander une relecture humaine. Avec `/gouchon:feature`, la
      revue de la phase Vérifier en tient lieu ; sans, ou si du code a changé depuis, lancer `/code-review`.
