# CLAUDE.md

Hubscore : API REST Spring Boot 3 (Java 21) qui enregistre les matchs de baby-foot et calcule l'ELO, les classements
et les statistiques. Base PostgreSQL migrée par Flyway, authentification déléguée à l'identity server (resource server
OAuth 2.0 / JWT). Le front `kicker-app` est le principal consommateur. Déploiement Docker sur un VPS.

## Avant de coder

La documentation du projet est dans `docs/`. Son sommaire est importé ci-dessous :

@docs/README.md

1. Lis le sommaire ci-dessus, puis ouvre **uniquement** le `README.md` du ou des dossiers utiles à ta tâche.
2. Dans ce dossier, charge seulement les fichiers dont la ligne « À lire si » correspond à ce que tu fais.
3. Pour une nouvelle feature, une évolution significative ou un correctif, utilise `/gouchon:feature` (plugin
   `claude-workflow`, profil du projet dans `.claude/workflow.md`) : cadrer, planifier, implémenter, vérifier, avec un
   fichier par phase dans `specs/<feature>/` (voir `specs/README.md`). La checklist de référence reste
   `docs/process/new-feature.md`.
4. Si le code existant contredit une convention, **la convention l'emporte** pour le nouveau code. L'écart est
   probablement déjà listé dans `docs/tech-debt.md` ; ajoute-le s'il n'y est pas.

## Commandes

```bash
./mvnw -B test         # toute la suite de tests (H2 en mémoire, sans Docker) : à lancer avant toute PR
make install           # worktree Orca : copie .env et config/application.yml, construit les images
make dev               # PostgreSQL + API dans Docker, API sur http://localhost:8080
make test              # tests dans Docker + résumé de couverture JaCoCo
make logs              # logs de l'API
```

## Règles non négociables

- **Couches** : controller → service → repository. Un controller n'appelle jamais un repository. Un service est
  toujours une interface `XxxService` avec son implémentation `service/impl/XxxServiceDefault`.
- **DTO** : les nouveaux DTO sont des `record` Java, validés par Jakarta Validation (`@Valid` sur le corps). **Jamais
  d'entité JPA** dans une requête ou une réponse.
- **Injection** : par constructeur uniquement, par l'interface (pas d'`@Autowired` ni de `@Value` sur un champ, pas
  de `new` d'un bean). La configuration passe par un `record` `@ConfigurationProperties` validé.
- **Entités** : pas de `@Setter` de classe ; une modification passe par une méthode d'intention qui vérifie la règle
  (`player.rename(...)`). Relations `LAZY`, `open-in-view` désactivé, conversion en DTO dans le service.
- **Sécurité** : tout `GET` sous `/api/v1/kicker/**` et `/api/v1/players/**` est **public** ; le reste est authentifié.
  Une route publique s'ajoute explicitement dans `SecurityConfig` ; une action réservée porte `@PreAuthorize`, un droit
  lié à la ressource se vérifie dans le service. Le backend est l'unique garant des droits.
- **Erreurs** : exceptions nommées traduites par `GlobalExceptionHandler` au format `ApiErrorResponseDTO` (`404`,
  `409`, `400`) ; un refus de droits n'est jamais traduit (Spring Security répond `401` / `403`) ; l'inattendu donne un
  `500` générique. Pas de `@Validated` sur un controller, pas de `try/catch` qui renvoie une `RuntimeException`.
- **Temps** : `java.time` et un `Clock` injecté ; jours et saisons découpés dans le fuseau `Europe/Paris` ; dates en ISO
  8601 dans l'API.
- **Base de données** : tout changement de schéma passe par une nouvelle migration Flyway
  `V<n>__<description>.sql`. On ne modifie jamais une migration déjà mergée.
- **Tests** : chaque service ou règle métier créé ou modifié a un test unitaire (Mockito, AssertJ), chaque endpoint
  créé ou modifié un test MockMvc (statut, droits, validation). Détail dans `docs/conventions/testing.md`.
- **Nommage** : on garde le schéma existant (packages `<domaine>_service`, entités au pluriel). Voir
  `docs/conventions/project-structure.md`.
- **Langue** : code, commentaires, messages d'erreur de l'API et commits en anglais ; documentation en français.
- **Git** : branche `<type>/<kebab>` depuis `dev`, Conventional Commits, PR vers `dev`. Ne jamais pousser sur `main`.

## Règles de la documentation

- **200 lignes visées** par fichier `.md` de `docs/` et pour ce `CLAUDE.md`. Au-delà, le hook du plugin `gouchon` le
  signale. Tu **proposes** alors le découpage à l'utilisateur avec `AskUserQuestion` : « Découper maintenant », « Pas
  maintenant » ou « Ne plus demander pour ce fichier ». Tu ne découpes jamais sans son accord. Les exceptions
  assumées sont listées dans `.claude/doc-length-ignore.txt`.
- **Découpage** : `x.md` devient un dossier `x/` avec un `README.md` (visée et sommaire) et des sous-fichiers.
  Le sommaire parent pointe ensuite vers `x/README.md`.
- **Sommaires** : chaque dossier de `docs/` a un `README.md`. Chaque entrée de sommaire donne un lien, une
  description et une ligne « À lire si ». Un nouveau fichier doit y être ajouté.
- **Mise à jour** : une PR qui change une convention, un process, l'architecture ou un endpoint met à jour la doc
  dans la même PR. Une décision structurante donne lieu à un ADR (`docs/decisions/`).
