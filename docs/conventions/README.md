# Conventions

**Visée** : les règles à suivre pour **tout nouveau code** et pour le code qu'on modifie. Elles partent des
recommandations de Spring Boot, Spring Data et Spring Security, adaptées au projet.

Chaque fichier suit le même schéma :

- la règle et sa justification en une phrase ;
- un exemple ;
- l'**état dans le code** : ✅ appliqué, ou ⚠️ écart listé dans [../tech-debt.md](../tech-debt.md).

**En cas de conflit** : la convention l'emporte sur le code existant. On ne recopie pas un pattern listé en dette. Si
une règle gêne réellement, on la change ici (dans la même PR, avec un ADR si elle est structurante) plutôt que de la
contourner.

**Quand ajouter un fichier ici** : quand un nouveau domaine de règles apparaît (cache, tâches planifiées, messages…).
Une règle isolée va dans le fichier existant le plus proche.

## Sommaire

- [code-style.md](code-style.md) : style Java, injection, Lombok, nommage, imports, logs, langue.
  _À lire si_ : tu écris du code Java, quel qu'il soit.
- [project-structure.md](project-structure.md) : packages par domaine, couches, où ranger chaque classe, nommage
  conservé (`<domaine>_service`, entités au pluriel).
  _À lire si_ : tu crées un fichier ou un package, ou tu te demandes où ranger du code.
- [api-design.md](api-design.md) : controllers, DTO `record`, validation, statuts HTTP, pagination, erreurs, droits,
  OpenAPI, compatibilité avec le front.
  _À lire si_ : tu crées ou modifies un endpoint ou un DTO.
- [persistence.md](persistence.md) : entités, repositories, requêtes, migrations Flyway, transactions, données
  dérivées (ELO, vue matérialisée).
  _À lire si_ : tu touches à une entité, une requête, une migration ou une écriture en base.
- [testing.md](testing.md) : ce qui doit être testé, tests unitaires de service, tests MockMvc, données de test,
  nommage.
  _À lire si_ : tu écris du code (chaque tâche de logique a son test) ou tu relis des tests.
- [git-workflow.md](git-workflow.md) : branches, Conventional Commits, PR vers `dev`, release vers `main`.
  _À lire si_ : tu crées une branche, un commit ou une PR.
