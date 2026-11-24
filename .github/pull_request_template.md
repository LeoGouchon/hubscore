## Résumé

<!-- Quoi et pourquoi, en 2-3 lignes. Lien vers l'issue, le besoin ou specs/<feature>/ s'il existe. -->

## Changements

-

## Contrat d'API

<!-- Endpoints ajoutés ou modifiés, droits, migrations. Signaler en premier tout changement qui casse kicker-app.
Supprimer la section si l'API ne change pas. -->

## Plan de test

<!-- Ce que tu as vérifié, pour que le relecteur puisse le reproduire. -->

- [ ] `./mvnw -B test` passe
- [ ] Tests ajoutés : service (unitaire) et endpoint (MockMvc) pour chaque logique touchée
- [ ] `make dev` : migrations appliquées, endpoints appelés (cas nominal et erreurs)
- [ ] Droits vérifiés : `401` sans jeton, `403` sans le rôle, succès avec
- [ ] Requête native nouvelle ou modifiée vérifiée sur PostgreSQL

## Checklist

- [ ] Titre de PR au format Conventional Commits (`type(scope): description`), base `dev`
- [ ] Conventions respectées (`docs/conventions/`), aucun nouvel écart listé dans `docs/tech-debt.md`
- [ ] Doc mise à jour : `docs/architecture/api.md` si un endpoint change, conventions, process ou architecture
      (fichiers ≤ 200 lignes, sommaires à jour)
- [ ] ADR ajouté si la PR contient une décision structurante (`docs/decisions/`)
- [ ] Points de `docs/tech-debt.md` corrigés au passage retirés de la liste
