# 0007. Tests PostgreSQL par Testcontainers, ignorés sans Docker

- **Statut** : Proposé
- **Date** : 2026-11-24

## Contexte

La suite de tests tourne sur H2 en mémoire (`application-test.yml`, Flyway désactivé, schéma créé par Hibernate).
Or le projet repose sur PostgreSQL ([ADR 0003](0003-postgresql-flyway.md)) : migrations Flyway, vue matérialisée
`mv_player_match_facts` et statistiques en SQL natif. H2 ne joue ni les migrations, ni la vue, et ne comprend pas tout
le SQL PostgreSQL : ces requêtes n'étaient vérifiées qu'à la main sur `make dev`.

La feature `hide-inactive-players` modifie deux requêtes natives du classement général : c'est le moment d'avoir un
test automatique sur un vrai PostgreSQL. La contrainte : `./mvnw -B test` doit rester utilisable sans Docker.

## Options envisagées

1. **Testcontainers, ignoré sans Docker** (`@Testcontainers(disabledWithoutDocker = true)`) : PostgreSQL 17 en
   conteneur, migrations appliquées par Flyway. La suite reste verte sans Docker ; le test est sauté en silence.
2. **Testcontainers obligatoire** : le test échoue sans Docker. Aucun saut silencieux, mais Docker devient un
   prérequis de `./mvnw -B test`.
3. **Vérification manuelle** sur `make dev` pendant la phase de vérification : rien à installer, mais rien
   d'automatique ni de rejouable en CI.

## Décision

Nous testons les requêtes natives, les migrations et la vue matérialisée sur PostgreSQL par **Testcontainers**, avec
des tests **ignorés quand Docker est absent**. Une classe de test de ce type :

- est annotée `@DataJpaTest(properties = "spring.config.location=classpath:/")` (pour ignorer un
  `config/application.yml` local), `@AutoConfigureTestDatabase(replace = Replace.NONE)`,
  `@ActiveProfiles({"test", "postgres"})` et `@Testcontainers(disabledWithoutDocker = true)` ;
- déclare `@Container @ServiceConnection static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")`
  (même version que `docker-compose.yml`) ;
- s'appuie sur le profil `src/test/resources/application-postgres.yml` (Flyway actif, dialecte PostgreSQL).

La raison principale : couvrir le SQL réel sans imposer Docker à qui lance seulement la suite unitaire.

## Conséquences

- **Positives** : les requêtes natives et la vue sont testées sur le même moteur qu'en production ; les migrations de
  `db/migration` sont rejouées à chaque exécution ; la CI (`ubuntu-latest`, Docker disponible) exécute ces tests.
- **Négatives / coûts** :
    - un développeur sans Docker ne voit pas ces tests tourner (ils apparaissent `Skipped`) ;
    - une CI sans Docker les sauterait sans échouer ;
    - la version de Testcontainers est fixée dans `pom.xml` (`testcontainers.version`) : celle de Spring Boot 3.4.2
      (1.20.x) ne fonctionne pas avec Docker Engine 29 et plus. À retirer quand Spring Boot gère une version récente.
- **Suivi** : recette dans [../conventions/testing.md](../conventions/testing.md#tests-de-requêtes) ; dans
  `tech-debt.md`, il reste à couvrir les autres requêtes natives et la vue.
