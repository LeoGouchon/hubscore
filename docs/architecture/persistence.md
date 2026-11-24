# Persistance

Le schéma PostgreSQL, sa gestion par Flyway, et les différentes façons dont le code lit et écrit les données.

## Fichiers clés

- `src/main/resources/db/migration/V<n>__*.sql` : migrations Flyway, seule source du schéma.
- `src/main/resources/db/dev-data/V9999__seed_dev_data.sql` : données fictives de dev (voir
  [../getting-started.md](../getting-started.md)).
- `*/entity/` : entités JPA ; `*/repository/` : repositories Spring Data.
- `kicker_match_service/service/impl/PlayerMatchFactsViewServiceDefault.java` : rafraîchit la vue matérialisée.
- `src/test/resources/application-test.yml` : base de test H2.

## Schéma

| Table                         | Entité                     | Rôle                                                               |
| ----------------------------- | -------------------------- | ------------------------------------------------------------------ |
| `users`                       | `Users`                    | compte : e-mail, `identity_user_id`, `role`, joueur lié (1-1)      |
| `players`                     | `Players`                  | joueur : prénom, nom                                               |
| `teams`, `player_team`        | `Teams`, `PlayerTeam`      | groupes de joueurs (flags `is_kicker`, `is_squash`), liaison N-N   |
| `player_kicker_informations`  | `PlayerKickerInformations` | ELO global et saisonnier courants du joueur, `@Version`            |
| `kicker_matches`              | `KickerMatches`            | match : 2 ou 4 joueurs, scores, `created_at`, `created_by`         |
| `kicker_elo`                  | `KickerElo`                | ELO global par (match, joueur) : avant, après, variation           |
| `kicker_elo_seasonal`         | `KickerEloSeasonal`        | idem pour l'ELO de la saison (année, trimestre)                    |
| `kicker_match_access_codes`   | `KickerMatchAccessCode`    | codes d'accès pour créer un match sans compte                      |
| `mv_player_match_facts`       | (vue matérialisée)         | une ligne par joueur et par match : partenaire, adversaires, score |

Les identifiants sont des UUID générés par Hibernate (`@GeneratedValue`). Les dates sont des colonnes `TIMESTAMP`
sans fuseau, lues en `java.sql.Timestamp` : elles sont écrites dans le fuseau de la JVM (UTC dans le conteneur
Docker). Dans le JSON, elles sortent en ISO 8601, sauf celles des codes d'accès, en millisecondes
(`@JsonFormat(shape = NUMBER)`) ; la cible est dans [../conventions/api-design.md](../conventions/api-design.md#dates-et-enums).

L'historique des migrations garde des traces du passé : tables squash renommées puis supprimées (V2, V20), tables
d'authentification locale supprimées au passage à l'identity server (V16, V19). Il n'y a pas de V15.

## Flyway et Hibernate

- Flyway applique les migrations au démarrage (`spring.flyway.*` dans `config/application.yml`).
- Hibernate est en `ddl-auto: validate` : il vérifie que les entités correspondent au schéma et **refuse de démarrer**
  sinon. Il ne crée ni ne modifie jamais de table.
- Les migrations ne sont jamais modifiées après merge : Flyway refuse une migration dont la somme de contrôle a changé.

## Lire les données

Quatre mécanismes coexistent :

1. **Requêtes dérivées** de Spring Data (`findByIdentityUserId`, `existsByCode`, `deleteByMatchCreatedAtAfter`).
2. **JPQL** avec `@Query` (`findLastElo`, `findAllByMatchIdIn`), et un verrou pessimiste sur la lecture d'un code
   d'accès (`findLockedByCode`, `@Lock(PESSIMISTIC_WRITE)`) pour compter ses usages sans course.
3. **SQL natif** (`nativeQuery = true`) pour les statistiques : classements, historique d'ELO, partenaires,
   adversaires, duos. Les résultats sont lus par des interfaces de projection (`repository/projection/`) ou
   directement dans des DTO. Beaucoup lisent `mv_player_match_facts`.
4. **Specifications** JPA pour les filtres dynamiques : `KickerMatchSpecifications` (joueurs, opérateurs `AND`, `OR`,
   `WITH`, `AGAINST`, date dans le fuseau `Europe/Paris`) et `PlayerSpecifications` (sport, team, sans compte).

## La vue matérialisée

`mv_player_match_facts` (V13) déplie chaque match en une ligne par joueur. Elle n'est pas à jour automatiquement :
`PlayerMatchFactsViewService` exécute `REFRESH MATERIALIZED VIEW` :

- après le commit d'une création ou d'une suppression de match (`refreshAfterCommit`) ;
- immédiatement après un recalcul complet de l'ELO (`refresh`).

Toute nouvelle écriture sur `kicker_matches` doit déclencher ce rafraîchissement.

## Transactions

Les écritures de service sont annotées `@Transactional` (Spring). Le recalcul complet de l'ELO tourne dans une
transaction séparée (`Propagation.REQUIRES_NEW`). L'ELO saisonnier vide le contexte de persistance après chaque
joueur (`flush` puis `clear`).

## Base de test

Les tests tournent sur **H2 en mémoire en mode PostgreSQL**, avec Flyway désactivé et `ddl-auto: create-drop` : le
schéma est généré depuis les entités. Les migrations, la vue matérialisée et les requêtes natives qui en dépendent ne
sont donc pas exercées par les tests (voir [../tech-debt.md](../tech-debt.md)).
