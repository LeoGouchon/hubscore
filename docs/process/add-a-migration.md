# Ajouter une migration

Changer le schéma ou les données de la base avec Flyway, sans casser la production. Règles :
[../conventions/persistence.md](../conventions/persistence.md).

## Fichiers clés

- `src/main/resources/db/migration/` : les migrations.
- `src/main/resources/db/dev-data/V9999__seed_dev_data.sql` : données de dev.
- `config/application.example.yml` : `spring.flyway.*` et `spring.jpa.hibernate.ddl-auto: validate`.

## Checklist

1. [ ] **Numéro** : `git fetch origin`, puis prendre le plus grand `V<n>` de `origin/dev` et créer
       `V<n+1>__<description_snake_case>.sql` (ex. `V21__add_nickname_to_players.sql`). Si une autre PR prend ce
       numéro avant la tienne, renuméroter au rebase (le fichier n'est pas encore appliqué en production).
2. [ ] **SQL PostgreSQL** écrit pour les données existantes :
    - colonne obligatoire : l'ajouter nullable, la remplir (`UPDATE`), puis poser `NOT NULL` (exemple : `V18`) ;
    - renommage ou suppression : vérifier qu'aucun code de la version précédente ne la lit encore au moment du
      déploiement ;
    - suppression de données : décidée dans le brief, jamais implicite.
3. [ ] **Contraintes et index** nommés explicitement (`fk_…`, `idx_…`, `chk_…`).
4. [ ] **Entité** alignée sur le schéma : Hibernate valide au démarrage (`ddl-auto: validate`).
5. [ ] **Vue matérialisée** : si `kicker_matches` change, vérifier `mv_player_match_facts` (V13) et la recréer par
       migration si besoin.
6. [ ] **Seed de dev** mis à jour si une colonne obligatoire est ajoutée. Tant qu'il reste en `V9999` (dette), une
       base locale qui l'a chargé ignore la nouvelle migration et refuse de démarrer : `make rebuild`. Cible : migration
       répétable `R__seed_dev_data.sql` ([../conventions/persistence.md](../conventions/persistence.md#migrations-flyway)).
7. [ ] **Vérifier sur PostgreSQL** : `make dev`, puis `make logs` (migration appliquée, démarrage sans erreur de
       validation). Pour rejouer depuis une base vide : `make rebuild` (**supprime les données locales**).
8. [ ] **Doc** : table ou colonne nouvelle dans [../architecture/persistence.md](../architecture/persistence.md).

## Ce qu'il ne faut pas faire

- Modifier une migration déjà mergée dans `dev` : Flyway refuse de démarrer (somme de contrôle). On écrit une
  nouvelle migration qui corrige.
- Compter sur les tests pour valider le SQL : ils tournent sur H2 avec le schéma généré par Hibernate, sans Flyway.
- Mettre des données de démonstration dans `db/migration/` : elles iraient en production.

## En cas de migration ratée en local

Flyway marque la migration en échec dans `flyway_schema_history`. En local, le plus simple est de corriger le fichier
(tant qu'il n'est pas mergé) et de repartir d'une base vide avec `make rebuild`.
