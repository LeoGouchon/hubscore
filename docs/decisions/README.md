# Décisions d'architecture (ADR)

**Visée** : garder la trace de **pourquoi** le projet est construit ainsi. Un ADR (Architecture Decision Record)
décrit :

- le contexte d'un choix structurant ;
- la décision prise et les options écartées ;
- ses conséquences.

On ne modifie pas un ADR accepté : on en écrit un nouveau qui le **remplace**, et on met à jour le statut de l'ancien.

**Quand écrire un ADR** :

- ajout ou retrait d'une librairie structurante (persistance, sécurité, tests…) ;
- nouveau pattern imposé à tout le code, ou changement d'un pattern existant ;
- choix d'infrastructure (hébergement, authentification, base, CI, déploiement) ;
- toute décision qu'un nouvel arrivant pourrait vouloir remettre en cause sans en connaître le contexte.

Pas d'ADR pour un choix local à une feature ni pour un détail de style : ceux-là vont dans les conventions.

**Comment** :

1. Copier [0000-template.md](0000-template.md) en `NNNN-titre-en-kebab.md`, avec le numéro suivant dans `origin/dev`
   à jour. Si une autre PR prend ce numéro avant la tienne, renuméroter au rebase (voir
   [../process/pull-request.md](../process/pull-request.md)).
2. Statut `Proposé` dans la PR, puis `Accepté` au merge.
3. Ajouter la ligne au sommaire ci-dessous.

Les ADR 0002 et 0003 documentent après coup des choix déjà en place : ils sont acceptés d'emblée. L'ADR 0004 en
documente aussi, mais y ajoute des règles nouvelles (entités riches, façade ELO) : il est proposé.

## Sommaire

| N°                                         | Décision                                                                                                       | Statut  |
| ------------------------------------------ | -------------------------------------------------------------------------------------------------------------- | ------- |
| [0001](0001-structure-documentation.md)    | Documentation dans `docs/`, sommaires progressifs, 200 lignes visées, `CLAUDE.md` comme point d'entrée IA      | Proposé |
| [0002](0002-resource-server-oidc.md)       | API en resource server JWT, comptes et identifiants gérés par l'identity server, rôles en base                 | Accepté |
| [0003](0003-postgresql-flyway.md)          | PostgreSQL, schéma géré par Flyway, Hibernate en `validate`, vue matérialisée pour les statistiques            | Accepté |
| [0004](0004-organisation-par-domaine.md)   | Packages par domaine, couches, service interface + `Default`, DTO `record`, entités riches, façade ELO         | Proposé |
| [0005](0005-workflow-feature.md)           | Workflow feature en quatre phases via le plugin `gouchon`, profil `.claude/workflow.md`, tests par tâche       | Proposé |
| [0006](0006-format-des-erreurs.md)         | Erreurs au format `ApiErrorResponseDTO` (pas ProblemDetail), statuts justes, refus de droits laissés à Spring  | Proposé |
| [0007](0007-tests-postgresql-testcontainers.md) | Tests PostgreSQL par Testcontainers (requêtes natives, migrations, vue), ignorés sans Docker      | Proposé |

_À lire si_ :

- **0001** : tu modifies la doc ou les instructions pour les agents ;
- **0002** : tu touches à l'authentification, aux rôles ou à l'identity server ;
- **0003** : tu envisages une autre base, un autre outil de migration, ou tu modifies la vue matérialisée ;
- **0004** : tu crées un package, un service ou un DTO, ou tu remets en cause le nommage ;
- **0005** : tu modifies le workflow `/gouchon:feature`, le profil ou la règle de test ;
- **0006** : tu touches à `GlobalExceptionHandler`, ajoutes une exception ou envisages ProblemDetail ;
- **0007** : tu écris un test de requête native ou de migration, ou tu touches à l'infrastructure de test.
