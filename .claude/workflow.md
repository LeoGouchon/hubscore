# Profil du workflow feature

Lu par les skills du plugin `gouchon` (`/gouchon:feature` et ses phases, voir
[l'ADR 0005](../docs/decisions/0005-workflow-feature.md)). Le déroulé des phases est dans le plugin ; ce fichier dit
seulement ce qui est propre à hubscore. En cas de conflit avec une skill, **ce profil et les conventions du projet
l'emportent**.

## Projet

- **Stack** : Spring Boot 3.4 (Java 21), Spring Web, Spring Data JPA, Spring Security (resource server JWT),
  PostgreSQL 17, Flyway, Lombok, SpringDoc ; tests JUnit 5, Mockito, AssertJ, MockMvc, H2.
- **Hors du dépôt** :
    - le front **kicker-app**, principal consommateur de l'API : changer un contrat existant (champ, statut, droit)
      peut le casser ;
    - l'**identity server**, qui émet les jetons, provisionne les utilisateurs (`POST /api/internal/identity-users`) et
      crée les invitations.

## Preuves automatiques

- `./mvnw -B test` → `BUILD SUCCESS`, 0 échec, 0 erreur (même commande que la CI ; H2 en mémoire, sans Docker).

## Tests

- **Règle** : chaque tâche qui crée ou modifie un service, une règle métier ou une requête complexe contient son test
  unitaire ; chaque tâche qui crée ou modifie un endpoint contient son test MockMvc (statut nominal, validation,
  droits). Le test peut s'écrire avant ou après le code, dans la même tâche.
- **Où et comment** : [docs/conventions/testing.md](../docs/conventions/testing.md) (miroir des packages dans
  `src/test/java`, noms `should_<comportement>_when_<condition>`, fabriques de `testutil/`).

## Cadrage

Branches propres au projet :

- **Contrat d'API** : chemin, méthode, paramètres, corps, réponse, codes d'erreur. Endpoint nouveau ou modifié ;
  compatibilité avec kicker-app (champ retiré ou renommé = rupture à planifier avec le front).
- **Sécurité** : public, authentifié ou réservé à un rôle (`USER`, `MODERATOR`, `ADMIN`) ; données d'un autre
  utilisateur exposées ou non.
- **Données** : nouvelle table ou colonne (migration Flyway), données existantes à migrer, effet sur l'ELO, les
  statistiques et la vue matérialisée `mv_player_match_facts` (recalcul, rafraîchissement).

**Contrats externes** : les besoins du front se lisent dans kicker-app (`src/hooks/`, `src/types/`) s'il est présent
à côté, mais c'est l'utilisateur qui valide le contrat. Un appel à l'identity server se décrit avec l'utilisateur :
son code n'est pas ici.

## Plan

- **Ordre des tâches** : migration Flyway → entité (méthodes d'intention et leurs tests) et repository → service
  (interface + `Default`) et son test → DTO `record` → controller, sécurité et test MockMvc → doc de l'API
  ([docs/process/new-feature.md](../docs/process/new-feature.md)). Recettes : `docs/process/add-an-endpoint.md`,
  `docs/process/add-a-migration.md`.
- **Structure** : [docs/conventions/project-structure.md](../docs/conventions/project-structure.md).
- **Où chercher l'existant** : `common/` (erreurs, pagination, configuration), les `service/` et `repository/` du
  domaine, `src/test/java/.../testutil/`.
- **Interdits à vérifier en auto-relecture** : entité JPA dans un DTO ou une réponse, controller qui appelle un
  repository, service sans interface, injection par champ, `@Setter` de classe sur une entité, `@Validated` sur un
  controller, migration existante modifiée, `GET` public non voulu sous `/api/v1/kicker/**` ou `/api/v1/players/**`.

## Vérification en conditions réelles

Appels HTTP sur l'API lancée en local :

1. `make dev` (PostgreSQL et API dans Docker). Les ports 5432 et 8080 sont partagés entre worktrees : si l'un est pris
   par un autre checkout, ne change pas de port, demande à l'utilisateur d'arrêter l'autre (`docker compose down`
   dans ce checkout) ; sinon note la vérification « non vérifié ». Attendre `GET /api/v1/ping` → `pong`.
2. **Endpoints publics** : les appeler avec `curl`, vérifier statut, corps et cas d'erreur du brief.
3. **Endpoints protégés** : sans jeton, vérifier qu'ils répondent `401`. Avec un jeton, seulement si l'utilisateur en
   fournit un (connexion via kicker-app, copie de l'en-tête `Authorization`) : ne jamais en fabriquer ni saisir
   d'identifiants. Sinon, ils vont dans sa checklist.
4. **Base** : vérifier les migrations appliquées (`make logs`, ou `docker compose exec db psql`) et les données
   écrites par la feature.
5. **Rôles** : la checklist de l'utilisateur liste, pour chaque rôle concerné, les appels qui doivent passer et ceux
   qui doivent renvoyer `403`.

## Points de revue

- **Sécurité** : `@PreAuthorize` sur chaque action réservée, cohérent avec le brief ; un `GET` sous
  `/api/v1/kicker/**` ou `/api/v1/players/**` est public par défaut (lecture réservée = `@PreAuthorize` + test `401`) ;
  droit lié à la ressource vérifié dans le service ; aucune route ajoutée à `permitAll` sans raison ; entrées validées
  (`@Valid` sur les objets imbriqués) ; aucun détail interne (message d'exception, SQL) dans une réponse.
- **Erreurs** : exception nommée et statut juste (`404`, `409`, `400`) ; refus de droits jamais attrapé par le
  handler ; inattendu en `500` ([ADR 0006](../docs/decisions/0006-format-des-erreurs.md)).
- **Contrat** : compatibilité avec kicker-app, codes HTTP justes (201 + `Location` à la création, 204 à la
  suppression), pagination au format `PaginatedResponseDTO`, dates en ISO 8601.
- **Données** : une transaction autour de chaque écriture multiple, `readOnly` sur les lectures, relations `LAZY` et
  conversion en DTO dans le service ; ELO et vue matérialisée à jour après une écriture de match (par la façade ELO) ;
  migration sans perte et compatible avec les données de production ; requête native vérifiée sur PostgreSQL
  (`make dev`), car les tests H2 ne la couvrent pas.
- **Objet** : entité modifiée par une méthode d'intention qui garde ses invariants (pas de `@Setter` de classe),
  logique métier hors du controller, plusieurs implémentations appelées ensemble derrière une façade.
- **Conventions** : entité exposée, controller → repository, service sans interface, `@Autowired` ou `@Value` sur un
  champ, `new` d'un bean, `@Validated` sur un controller, nom pleinement qualifié, `RuntimeException` générique,
  `jakarta.transaction.Transactional`, `Date` / `Timestamp` / `Calendar` ou `now()` sans `Clock`, `System.out`, code
  commenté.
