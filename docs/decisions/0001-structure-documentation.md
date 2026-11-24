# 0001. Structure de la documentation

- **Statut** : Proposé
- **Date** : 2026-11-24

## Contexte

La doc du projet se limitait à un README en anglais, en partie périmé (il cite `jwt.secret`, une authentification
locale et Spring Security Crypto, remplacés par l'identity server). Les conventions n'étaient écrites nulle part : un
nouveau code recopiait le fichier le plus proche, y compris ses écarts.

Le projet est développé avec des agents IA (Claude Code), comme kicker-app, qui a adopté la même structure. Un agent
a besoin d'une doc qu'il peut charger **par morceaux** : un fichier unique et long consomme le contexte pour rien, et
une doc éparpillée sans index n'est pas trouvée.

## Options envisagées

1. **Garder le README** et l'enrichir : simple, mais il grossit et doit être lu en entier.
2. **Diátaxis** (tutorials / how-to / reference / explanation) : référence reconnue, trop lourde pour ce projet.
3. **Dossiers par thème avec sommaires progressifs**, comme kicker-app.

## Décision

Nous retenons l'option 3, identique à kicker-app pour qu'on passe d'un dépôt à l'autre sans réapprendre :

- **Organisation** : `docs/` est la source de vérité, en français : `architecture` (ce qui est), `conventions` (ce qui
  doit être), `process` (comment faire), `decisions` (pourquoi), plus `glossary.md` et `tech-debt.md`.
- **Sommaires** : un `README.md` par dossier, chaque entrée avec un lien, une description et une ligne « À lire si ».
- **Taille** : 200 lignes visées par fichier. Le hook du plugin `gouchon` signale le dépassement et l'agent propose un
  découpage ; les exceptions assumées vont dans `.claude/doc-length-ignore.txt`.
- **Point d'entrée IA** : `CLAUDE.md` importe `docs/README.md` et résume les règles non négociables.
- **Cible plutôt que constat** : les conventions décrivent la cible ; les écarts du code actuel sont listés dans
  `tech-debt.md` au lieu d'être documentés comme normes.
- **README** : réduit au démarrage rapide, en français, avec des liens vers `docs/`.

## Conséquences

- **Positives** : un agent ne charge que ce qui sert sa tâche ; les règles sont explicites ; la dette est visible et
  priorisée.
- **Coûts** :
    - la doc se met à jour dans la même PR que le code qui la contredit, en particulier
      `architecture/api.md` à chaque endpoint ;
    - le hook ne surveille que les écritures faites par Claude Code : la relecture de PR vérifie la taille et
      `doc-length-ignore.txt`.
- **Suivi** : migrer le code vers les conventions au fil de `tech-debt.md`.
