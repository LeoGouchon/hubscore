# Glossaire

Le vocabulaire métier de Hubscore, avec le terme français et son nom dans le code (anglais). C'est la référence pour
les briefs, l'API et les échanges : un terme défini ici ne s'emploie pas dans un autre sens. Il est commun avec
kicker-app : un terme ajouté ou corrigé ici l'est aussi là-bas.

## Fichiers clés

- `*/entity/*.java` et `*/dto/*.java` : les noms du code.
- skill `gouchon:cadrer` (plugin `claude-workflow`) : la phase de cadrage lit ce glossaire et le complète.

## Règles

- **Un concept, un terme.** Les synonymes à ne pas employer sont listés sous _À éviter_.
- **Une ou deux phrases** par définition : ce que la chose **est**, pas comment elle est codée.
- **Métier uniquement** : pas de notion technique générale (pagination, cache, token…).
- **_À confirmer_** : le terme ou la définition a été déduit du code et attend une validation lors d'un prochain
  cadrage.

## Joueurs et comptes

**Joueur** (`Players`) :
Une personne qui joue au baby-foot et dont on enregistre les matchs. Un joueur peut exister sans compte.
_À éviter_ : utilisateur, membre.

**Utilisateur** (`Users`) :
Un compte qui se connecte à l'application, avec un e-mail et un rôle. Il peut être lié à un joueur. Le compte
lui-même (identifiants, connexion) vit dans l'identity server ; hubscore en garde le rôle et le lien au joueur.
_À éviter_ : joueur (quand on parle du compte), membre.

**Rôle** (`UserRole`) :
Le niveau de droits d'un utilisateur : `USER`, `MODERATOR` (modérateur) ou `ADMIN`. Un visiteur non connecté est dit
**anonyme**.

**Invitation** (`InvitationRequest`, `InvitationResponse`) :
Le lien envoyé pour qu'une personne crée son compte et le relie à un joueur qui n'a pas encore de compte.
_À éviter_ : parrainage.

**Team** (`Teams`) :
Un groupe de joueurs, comparable à un club, choisi à la création d'un joueur. Elle est appelée à représenter un
service de l'entreprise. Historiquement liée à un sport, d'où le filtre « kicker » encore présent dans l'API
(flags `isKicker` et `isSquash`).
_À éviter_ : équipe (réservé aux deux camps d'un match), service (tant que le renommage n'est pas fait).

## Matchs

**Match** (`KickerMatches`) :
Une partie de baby-foot entre l'équipe A et l'équipe B, gagnée par la première équipe qui atteint 10 buts.

**Équipe** (A ou B, `player1A`, `player2A`, `player1B`, `player2B`) :
L'un des deux camps d'un match, composé d'un ou deux joueurs. Le match est donc en 1 contre 1 ou en 2 contre 2.
_À éviter_ : team (c'est un autre concept), côté.

**Score** (`scoreA`, `scoreB`) :
Le nombre de buts de chaque équipe. L'un des deux vaut exactement 10.

**Partenaire** (`PartnerStatsDTO`) :
Un joueur qui était dans la même équipe qu'un autre joueur pendant un match.
_À éviter_ : coéquipier (sauf dans les noms techniques existants, comme `teammate_id`).

**Adversaire** (`OpponentStatsDTO`) :
Un joueur qui était dans l'équipe opposée.

**Duo** (`DuoStatsDTO`) :
Une paire de joueurs, considérée à travers les matchs qu'ils ont joués ensemble dans la même équipe.

**Code d'accès** (`KickerMatchAccessCode`) :
Un code créé par un admin qui permet d'enregistrer un match sans être connecté. Il peut expirer ou être **révoqué**.
_À éviter_ : code match (libellé actuel du menu de kicker-app, à harmoniser), mot de passe.

## Classement et ELO

**ELO** :
La note de niveau d'un joueur. Elle monte quand il gagne, baisse quand il perd, et varie d'autant plus que le résultat
était inattendu et l'écart de buts grand. Tout joueur part d'une valeur de départ fixée par le backend (1500
aujourd'hui) : l'interface ne la suppose jamais.

**ELO global** (`KickerElo`, `playerCurrentElo`) :
L'ELO calculé sur tous les matchs depuis le début.

**ELO saisonnier** (`KickerEloSeasonal`, `playerCurrentSeasonalElo`) :
L'ELO calculé uniquement sur les matchs d'une saison ; il repart de la valeur de départ à chaque saison.

**Gain d'ELO** (`eloChange`, `eloWinTeamA`, `eloWinTeamB`) :
Les points d'ELO gagnés ou perdus par un joueur, ou par chaque joueur d'une équipe, sur un match.
_À éviter_ : delta (seul), points. `deltaElo` est un champ déprécié de l'API.

**Chance de victoire** (`winChanceTeamA`, `winChanceTeamB`) :
La probabilité de victoire de chaque équipe avant le match, déduite de l'ELO de ses joueurs.

**Joueur actif** :
Un joueur qui a joué au moins un match dans les 90 jours précédant la date de référence.

**Joueur classé** (`MIN_RANKED_MATCHES`) :
Un joueur qui apparaît au classement. Au classement depuis le début : au moins 10 matchs et actif. Au classement d'une
saison : au moins 10 matchs dans la saison. Son ELO n'est montré qu'à partir de 10 matchs, qu'il soit actif ou non.

**Saison** (`SeasonStatsResponseDTO`, `seasonYear`, `seasonQuarter`) :
Un trimestre civil (année et trimestre T1 à T4). Les classements et l'ELO saisonnier sont calculés par saison.
_À éviter_ : période, année.

**Classement** (`rank`) :
La position d'un joueur parmi les joueurs classés, triés par ELO, sur la saison ou depuis le début.
_À éviter_ : ranking, rang.

**Taux de victoire** (`winRate`) :
La part de matchs gagnés parmi les matchs joués.
_À éviter_ : ratio (libellé actuel de certaines colonnes de kicker-app), pourcentage victoire.
