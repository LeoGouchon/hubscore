# 0003. PostgreSQL et Flyway, statistiques en SQL

- **Statut** : Accepté
- **Date** : 2026-11-24 (choix en place depuis l'origine du projet, vue matérialisée depuis V13)

## Contexte

L'API stocke des joueurs, des matchs et un historique d'ELO par match, et sert des statistiques agrégées
(classements, partenaires, adversaires, duos, historique par saison). Le schéma évolue souvent et doit évoluer de la
même façon en local, en CI et en production.

## Options envisagées

1. **Schéma généré par Hibernate** (`ddl-auto: update`) : rien à écrire, mais aucune maîtrise des changements ni des
   données existantes.
2. **Migrations versionnées** (Flyway ou Liquibase) et Hibernate en simple validation.
3. **Statistiques calculées en Java** à partir des entités, ou **en SQL** dans la base.

## Décision

- **PostgreSQL 17** comme base unique.
- **Flyway** : le schéma n'évolue que par des migrations SQL versionnées (`V<n>__*.sql`), appliquées au démarrage ;
  Hibernate est en `ddl-auto: validate` et refuse de démarrer si une entité ne correspond pas.
- **Statistiques en SQL natif** dans les repositories, appuyées sur une vue matérialisée (`mv_player_match_facts`)
  qui déplie chaque match en une ligne par joueur, rafraîchie après chaque écriture de match.

## Conséquences

- **Positives** : schéma identique partout et historisé ; agrégations rapides et lisibles en SQL.
- **Négatives / coûts** :
    - une migration mergée ne se modifie plus ; une erreur se corrige par une nouvelle migration ;
    - le SQL natif et la vue sont propres à PostgreSQL : les tests sur H2 ne les couvrent pas ;
    - la vue doit être rafraîchie explicitement, et son rafraîchissement complet coûte à chaque écriture de match.
- **Suivi** : tests d'intégration sur PostgreSQL (Testcontainers) listés dans [../tech-debt.md](../tech-debt.md).
