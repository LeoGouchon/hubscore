# Pull request

Préparer, ouvrir, relire et merger une PR vers `dev`. Les règles de branches et de commits sont dans
[../conventions/git-workflow.md](../conventions/git-workflow.md).

## Avant d'ouvrir

- [ ] Branche à jour avec `dev` (`git fetch && git rebase origin/dev`). Conflits fréquents entre branches parallèles :
    - `docs/glossary.md`, sommaire des ADR, `docs/tech-debt.md`, `docs/architecture/api.md` : garder les entrées des
      deux côtés ;
    - un ADR ou une migration de la branche porte un numéro déjà pris dans `dev` : le renuméroter (nom du fichier,
      titre, sommaire, liens qui y pointent, y compris dans `specs/`).
- [ ] `./mvnw -B test` passe.
- [ ] Test manuel sur `make dev` fait : endpoints touchés, erreurs, droits par rôle (voir
      [new-feature.md](new-feature.md#4-vérifier)).
- [ ] Auto-relecture du diff complet (`git diff origin/dev...`) :
    - [ ] pas de fichier parasite, de `System.out`, de code commenté, de secret ;
    - [ ] pas d'entité exposée, de controller qui appelle un repository, de route publique non voulue.
- [ ] Doc mise à jour si une convention, un process, l'architecture ou un endpoint change. Chaque fichier reste sous
      200 lignes, sauf exception assumée dans `.claude/doc-length-ignore.txt`.
- [ ] Avec Claude Code : revue faite et ses retours traités. Avec `/gouchon:feature`, c'est la revue de la phase
      Vérifier (`specs/<feature>/4-verification.md`) ; sans, ou si du code a changé depuis, `/code-review`.

## Ouvrir

- **Base** : `dev`. Jamais `main`, sauf pour la PR de release ([release.md](release.md)).
- **Titre** : au format Conventional Commits, en anglais, car il deviendra le message du commit squashé et décidera
  de la version. Exemple : `feat(kickerStats): add duo stats per season`.
- **Description** : remplir `.github/pull_request_template.md` (résumé, contrat d'API, plan de test).
- **Taille** : viser une PR relisible en une fois. Au-delà de ~400 lignes de diff, sans compter `specs/` ni les
  données de seed, découper si possible (refactor préalable dans une PR à part, puis la feature).
- **Contrat cassé** pour kicker-app : le signaler en tête de la description et coordonner la PR du front.

```bash
gh pr create --base dev --title "feat(kickerStats): add duo stats per season" --body-file <fichier>
```

La CI (`tests.yml`) lance `./mvnw -B test` sur la PR : elle doit être verte avant le merge.

## Relire

Le relecteur vérifie, dans cet ordre :

1. **Comportement** : le besoin est couvert, le plan de test est crédible et reproductible.
2. **Sécurité** : droits de chaque route cohérents avec le besoin, aucune route publique de trop, entrées validées,
   aucun détail interne dans les erreurs.
3. **Données** : migration sans perte, transactions, ELO et vue matérialisée à jour après une écriture de match.
4. **Contrat** : statuts HTTP, format des réponses, compatibilité avec kicker-app.
5. **Tests** : chaque service et chaque endpoint touchés ont leurs tests, et ils échoueraient si la règle cassait.
6. **Conventions** et **dette** : la PR n'ajoute pas d'écart listé dans [../tech-debt.md](../tech-debt.md), et
   retire ceux qu'elle corrige.

Commentaires : distinguer ce qui bloque (bug, faille, convention violée) de la suggestion (préfixe `nit:`).

## Merger

- **Mode** : **squash merge** dans `dev`, en gardant le titre de la PR comme message (GitHub ajoute `(#NN)`).
- **Après le merge** : supprimer la branche distante et locale.
- **Si `dev` a bougé pendant la relecture** : rebaser, relancer `./mvnw -B test`, puis merger.
