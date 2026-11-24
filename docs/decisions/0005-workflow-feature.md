# 0005. Workflow feature en quatre phases avec le plugin `gouchon`

- **Statut** : Proposé
- **Date** : 2026-11-24

## Contexte

kicker-app mène ses features avec un workflow en quatre phases (ADR 0006 et 0008 de kicker-app) : le besoin est
construit avec le développeur avant le code, le plan est écrit et validé, la vérification apporte des preuves, et
chaque étape laisse un fichier dans `specs/<feature>/` qui permet de reprendre après une coupure. Ce workflow a été
extrait dans un plugin Claude Code privé, `gouchon` (dépôt `LeoGouchon/claude-workflow`), pour servir à tous les
projets. Hubscore n'avait aucun process écrit.

## Options envisagées

1. **Pas de workflow outillé** : la checklist `docs/process/new-feature.md` seule.
2. **Copier les skills de kicker-app** dans ce dépôt : elles divergeraient, et elles parlent de React et de navigateur.
3. **Le plugin `gouchon`**, avec un profil propre à hubscore.

## Décision

Nous retenons l'option 3 :

- `.claude/settings.json` déclare le marketplace `claude-workflow` et active `gouchon@claude-workflow` ;
- `.claude/workflow.md` est le **profil** de hubscore : preuve automatique `./mvnw -B test`, règle de test, branches
  de cadrage (contrat d'API, sécurité, données), ordre des tâches, vérification par appels HTTP sur `make dev`, points
  de revue ;
- **règle de test** : chaque tâche qui touche un service ou une règle métier a son test unitaire, chaque endpoint son
  test MockMvc ; le test s'écrit dans la même tâche, avant ou après le code (pas de TDD imposé) ;
- les fichiers `specs/<feature>/` sont commités dans la branche de la feature et gardés après le merge ;
- **vérification en conditions réelles** : l'agent appelle les endpoints publics et vérifie les `401` ; les appels
  authentifiés demandent un jeton fourni par le développeur, jamais fabriqué par l'agent, et les rôles restent dans la
  checklist du développeur.

## Conséquences

- **Positives** : même workflow que kicker-app ; chaque PR arrive avec son besoin, son plan, ses tests et ses preuves.
- **Négatives / coûts** :
    - le plugin s'installe sur chaque machine (accès Git au dépôt privé requis) ;
    - quatre fichiers par feature, même petite ;
    - la vérification par rôle reste manuelle tant qu'aucun jeton de test n'est disponible en local.
- **Suivi** : une évolution du workflow se fait dans le dépôt du plugin ; une évolution propre à hubscore, dans le
  profil.
