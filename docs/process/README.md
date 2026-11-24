# Process

**Visée** : les recettes pas-à-pas pour livrer, sous forme de checklists actionnables. Elles disent **dans quel
ordre faire quoi** et renvoient vers [conventions/](../conventions/README.md) pour les règles, sans les répéter.

**Quand ajouter un fichier ici** : quand une tâche revient régulièrement et qu'elle a des étapes faciles à oublier
(ajout d'une propriété de configuration, montée de version de Spring Boot…).

## Sommaire

- [new-feature.md](new-feature.md) : le parcours complet d'une feature, du cadrage au merge, et son déroulé avec
  Claude Code (`/gouchon:feature`).
  _À lire si_ : tu démarres une nouvelle feature, une évolution significative ou un correctif. **Point d'entrée par
  défaut.**
- [add-an-endpoint.md](add-an-endpoint.md) : créer un endpoint (DTO, service, controller, sécurité, tests, doc), avec
  un squelette de code.
  _À lire si_ : ta tâche ajoute ou modifie une route de l'API.
- [add-a-migration.md](add-a-migration.md) : changer le schéma de la base avec Flyway sans casser la production.
  _À lire si_ : ta tâche ajoute ou modifie une table, une colonne, un index ou des données.
- [pull-request.md](pull-request.md) : préparer, ouvrir, relire et merger une PR vers `dev`.
  _À lire si_ : ton code est prêt à être proposé, ou tu relis la PR de quelqu'un.
- [release.md](release.md) : livrer `dev` en production via la PR vers `main` (semantic-release, Docker, VPS).
  _À lire si_ : tu prépares une mise en production.
