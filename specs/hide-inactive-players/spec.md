# Spec backend : retirer les joueurs inactifs du classement général

- **Statut** : validé
- **Branche** : `feat/hide-inactive-players`
- **Date** : 2026-11-24
- **Front lié** : kicker-app, branche `feat/hide-inactive-players`, `specs/hide-inactive-players/1-brief.md`

## Contexte

Le classement général (`GET /api/v1/kicker/stats/global`) classe tous les joueurs qui ont au moins 10 matchs depuis le
début, même s'ils ne jouent plus depuis des mois. Ces joueurs figés occupent des places et faussent la lecture du
classement. On veut qu'un joueur qui ne joue plus sorte du classement général, sans perdre son ELO ni son historique,
et qu'il y revienne dès qu'il rejoue.

## Vocabulaire

- **Joueur actif** : un joueur qui a joué au moins un match dans les 90 jours précédant la date de référence.
- **Classé** (classement général) : au moins 10 matchs au total (`MIN_RANKED_MATCHES`, inchangé) **et** actif.
- **Non classé** : tous les autres joueurs. Ils restent dans la réponse, avec `rank = 0`.

## Règle

### Classement actuel (`rank`, `currentElo`)

- **Date de référence** : maintenant.
- **`rank`** : le `RANK()` actuel (par ELO global décroissant) est calculé **uniquement** parmi les joueurs classés
  (au moins 10 matchs **et** actifs). Les autres ont `rank = 0`. Les inactifs ne laissent donc pas de trou dans la
  numérotation. Les égalités restent gérées comme aujourd'hui (`RANK()` : 1, 1, 3).
- **`currentElo`** : **inchangé**. Il est renvoyé dès que le joueur a au moins 10 matchs, actif ou non, et vaut `null`
  en dessous. Un joueur inactif avec 10 matchs ou plus a donc `rank = 0` **et** un `currentElo` renseigné : c'est ce qui
  permet au front de le distinguer d'un joueur qui a moins de 10 matchs.

### Classement de référence (`rankLastWeek`, `eloLastWeek`)

- **Date de référence** : la coupure actuelle (lundi 00:00 de la semaine en cours), inchangée.
- **`rankLastWeek`** : même règle que `rank`, évaluée à la coupure. Le `RANK()` est calculé parmi les joueurs qui
  avaient, à la coupure, au moins 10 matchs **et** au moins un match dans les 90 jours avant la coupure. Pour les
  autres, `null` (comme aujourd'hui pour les joueurs sous le seuil).
- **`eloLastWeek`** : **découplé du rang**. Il vaut l'ELO après le dernier match avant la coupure pour **tout** joueur
  qui avait au moins 10 matchs à la coupure, **actif ou non** à cette date. Sinon, `null`.

Pourquoi ce découplage : un joueur qui revient après plus de 90 jours d'absence n'avait pas de rang lundi
(`rankLastWeek = null`). Le front doit quand même afficher son gain ou sa perte de points depuis lundi
(`currentElo - eloLastWeek`) au lieu de le présenter comme un nouveau classé.

### Comportement attendu (date de référence : maintenant)

| Joueur                                               | `rank` | `currentElo` | `rankLastWeek`  | `eloLastWeek`    |
| ---------------------------------------------------- | ------ | ------------ | --------------- | ---------------- |
| 50 matchs, dernier il y a 3 jours                    | > 0    | ELO          | rang de lundi   | ELO de lundi     |
| 50 matchs, dernier il y a 120 jours                  | 0      | ELO          | `null`          | ELO de lundi     |
| 50 matchs, revenu hier après 6 mois sans jouer       | > 0    | ELO          | `null`          | ELO de lundi     |
| 6 matchs, dernier hier                               | 0      | `null`       | `null`          | `null`           |
| 9 matchs lundi, 10e match hier (vient d'être classé) | > 0    | ELO          | `null`          | `null`           |

## Contrat

- **Endpoint** : `GET /api/v1/kicker/stats/global` uniquement.
- **DTO** : `GlobalStatsWithHistoryDTO` **inchangé**. Aucun champ ajouté ni retiré.
- **Ordre de la réponse** : inchangé.
- **Droits** : inchangés.

## Pistes d'implémentation

À titre indicatif, d'après la lecture du code (`kicker_match_service`) :

- **Constante** : ajouter `ACTIVE_PLAYER_WINDOW_DAYS = 90` à côté de `MIN_RANKED_MATCHES`, dans
  `service/KickerEloVisibilityRules.java`.
- **Requêtes** : modifier les deux requêtes du classement général dans `repository/KickerMatchRepository.java` :
  - `getGlobalKickerStats` : filtre d'activité dans le `RANK()` ;
  - `getLatestKickerEloByDate` : filtre d'activité pour `rankLastWeek` uniquement. L'ELO de lundi est renvoyé pour
    tout joueur au-dessus du seuil.

  Le dernier match d'un joueur se lit avec `MAX(match_date)` dans `mv_player_match_facts`, qui a déjà l'index
  `(player_id, match_date DESC)`.
- **Fuseau horaire** : `created_at` est un `TIMESTAMP` sans fuseau, écrit avec l'heure de la JVM. Calculer la borne
  « référence moins 90 jours » côté Java et la passer en paramètre (`:activeSince`), plutôt que d'utiliser `now()` en
  SQL, pour rester cohérent avec l'écriture.
- **Service** : `service/impl/KickerStatServiceDefault.getGlobalStats` alimente aujourd'hui `rankLastWeek` et
  `eloLastWeek` avec une seule requête qui filtre déjà sur le seuil. Il faudra soit deux résultats, soit une requête
  qui renvoie le rang (éventuellement `null`) et l'ELO séparément.

## Cas limites

- **Limite des 90 jours** : un match joué exactement à la borne compte (borne incluse : `match_date >= activeSince`).
- **Aucun joueur actif** : tous les joueurs ont `rank = 0`. La réponse n'est pas vide.
- **Joueur inactif avec moins de 10 matchs** : `rank = 0`, `currentElo = null`. Il ne se distingue pas d'un nouveau
  joueur, et c'est voulu.
- **Match supprimé** : si c'était le seul match récent d'un joueur, il redevient inactif au prochain appel (la vue
  matérialisée est déjà rafraîchie après une suppression).

## Critères d'acceptation

- [ ] Un joueur sans match depuis plus de 90 jours a `rank = 0` dans `/global`, et son `currentElo` reste renseigné
      s'il a au moins 10 matchs.
- [ ] Les rangs de `/global` se suivent sans trou laissé par les inactifs (hors égalités).
- [ ] Un joueur qui rejoue après plus de 90 jours retrouve un rang dès son match suivant.
- [ ] Pour ce joueur, `rankLastWeek = null` et `eloLastWeek` vaut son ELO à la coupure de lundi.
- [ ] `rankLastWeek` applique la règle d'activité évaluée à la coupure de lundi.
- [ ] Les endpoints `/season/{year}/{quarter}`, `/season`, `/player/{id}`, `/duo` et `/matrix-score` renvoient
      exactement la même chose qu'avant.
- [ ] Le DTO de `/global` est inchangé.
- [ ] Un test couvre les deux requêtes du classement général sur PostgreSQL. Les tests actuels tournent sur H2, qui ne
      sait pas exécuter ces requêtes (Testcontainers ou équivalent). Il couvre au minimum les cinq cas du tableau
      ci-dessus.

## Hors périmètre

- Les classements saisonniers (`/season/{year}/{quarter}`) : un non classé y reste un joueur avec moins de 10 matchs
  dans la saison.
- La constante `MIN_RANKED_MATCHES` et ses autres usages : historique ELO de la fiche joueur, ELO affiché dans la
  liste des matchs.
- Le calcul de l'ELO : les matchs des joueurs inactifs comptent toujours.
- Une notion de joueur archivé ou masqué.

## Signalé au passage (non traité ici)

- **Coupure du lundi** : elle est construite avec `Calendar.getInstance()`, qui dépend de la locale du serveur. Avec
  une locale où la semaine commence le dimanche, un dimanche, la coupure tombe le lundi **suivant**. Les valeurs « de
  la semaine dernière » sont alors égales aux valeurs actuelles.
- **N+1** : `lastMatches` est chargé par une requête par joueur dans `getGlobalStats` et `getSeasonStats`.
- **`lastMatches` sur une saison** : il n'est pas filtré par saison. L'endpoint d'une saison renvoie les 5 derniers
  matchs tous temps confondus.
