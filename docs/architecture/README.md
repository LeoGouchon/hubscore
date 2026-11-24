# Architecture

**Visée** : décrire comment le code fonctionne **aujourd'hui** : flux, responsabilités et fichiers qui font foi.
Ce dossier ne fixe pas de règles (c'est le rôle de [conventions/](../conventions/README.md)). Il explique ce qui
existe, pour qu'on puisse le modifier sans le casser.

**Quand ajouter un fichier ici** : quand un mécanisme transverse (utilisé par plusieurs domaines ou endpoints) mérite
une explication : un nouveau calcul dérivé, une intégration externe, une tâche planifiée. Une feature isolée n'a pas
sa place ici.

## Sommaire

- [overview.md](overview.md) : stack, domaines (`user_service`, `player_service`, `kicker_match_service`, `common`),
  couches, parcours d'une requête, gestion des erreurs.
  _À lire si_ : tu découvres le projet ou tu cherches où se trouve quelque chose.
- [security.md](security.md) : resource server JWT, rôles, routes publiques, provisioning et invitations via
  l'identity server, CORS.
  _À lire si_ : tu touches à l'authentification, à un rôle, à une route publique ou à l'identity server.
- [persistence.md](persistence.md) : schéma, entités, migrations Flyway, requêtes (dérivées, JPQL, natives,
  specifications), vue matérialisée, base de test.
  _À lire si_ : tu ajoutes une table, une colonne, une requête ou une migration.
- [elo-and-stats.md](elo-and-stats.md) : calcul de l'ELO global et saisonnier, recalcul, règle de visibilité,
  statistiques, codes d'accès.
  _À lire si_ : tu touches à la création ou suppression de match, à l'ELO, aux classements ou aux statistiques.
- [api.md](api.md) : liste des endpoints avec leurs droits, pagination, format d'erreur, OpenAPI.
  _À lire si_ : tu ajoutes ou modifies un endpoint, ou tu cherches le contrat d'un appel du front.
