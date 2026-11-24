# Journal d'exécution : retirer les joueurs inactifs du classement général

- **Statut** : en cours (cycle des statuts : `specs/README.md`)
- **Plan** : [2-plan.md](2-plan.md)

Le diff de la feature se calcule par `git diff origin/dev...HEAD` (depuis la base commune avec `dev`) : il reste
juste après un rebase, contrairement à un SHA noté ici.

## Tâches

Une ligne par tâche, ajoutée quand la tâche est faite. Une tâche absente n'est pas faite.

- **Tâche 1** : faite. Commit `test(kicker): add Testcontainers PostgreSQL setup for native queries`. Vérification :
  `./mvnw -B test` → `BUILD SUCCESS`, 56 tests, 0 échec ; Docker lancé, `KickerMatchRepositoryTests` `Tests run: 1,
  Skipped: 0`.

## Décisions

Chaque écart au plan ou ambiguïté tranchée, au moment où elle est prise :

- **Tâche 1** : `testcontainers.version` fixée à `1.21.4` dans `pom.xml` au lieu de la version gérée par Spring Boot
  3.4.2 (1.20.4) ; avec Docker Engine 29 (API minimale 1.44), la 1.20.4 ne trouve aucun environnement Docker et le test
  est ignoré. Ligne ajoutée à `tech-debt.md` pour retirer la propriété à la montée de Spring Boot. Si c'est faux : un
  conflit de versions Testcontainers à la montée de Spring Boot, visible à la compilation des tests.
- **Tâche 1** : ADR 0007 au statut **Proposé** et non « Accepté » comme le dit le plan : `docs/decisions/README.md`
  impose `Proposé` dans la PR, puis `Accepté` au merge (la convention l'emporte). Coût si c'est faux : un mot à changer.

## Raccourcis

Chaque commentaire `shortcut:` laissé dans le code :

## Bilan

À remplir quand toutes les tâches sont faites : ce qui a été construit en trois lignes, et ce que la vérification doit
regarder en priorité.
