# ELO et statistiques

Comment un match fait évoluer l'ELO global et saisonnier, quand tout est recalculé, et d'où viennent les classements
et statistiques.

## Fichiers clés

- `kicker_match_service/service/impl/EloCalculatorDefault.java` : les formules.
- `kicker_match_service/service/impl/GlobalKickerEloService.java` (`@Qualifier("globalEloService")`) et
  `SeasonalKickerEloService.java` (`@Qualifier("seasonalEloService")`) : deux implémentations de `KickerEloService`.
- `kicker_match_service/service/impl/KickerMatchServiceDefault.java` : création, suppression, lecture des matchs.
- `kicker_match_service/service/KickerEloVisibilityRules.java` : `MIN_RANKED_MATCHES = 10`.
- `kicker_match_service/service/impl/KickerStatServiceDefault.java`, `EloMatrixServiceDefault.java` : statistiques.
- `kicker_match_service/service/impl/KickerMatchAccessCodeServiceDefault.java` : codes d'accès.

## Les formules

Pour un match entre l'équipe A et l'équipe B (1 contre 1 ou 2 contre 2) :

- **ELO d'équipe** : moyenne de l'ELO courant de ses joueurs (1500 pour un joueur sans historique).
- **Résultat attendu** : `1 / (1 + 10^((eloB - eloA) / 600))`.
- **Score réel** : pas 1 ou 0, mais un facteur entre 0,7 et 1 pour le vainqueur, selon l'écart relatif de score
  (`0.7 + 0.3 * (écart / score max)^0.1`) ; le perdant reçoit le complément.
- **Facteur K** : dépend de l'écart de buts, `round(15 * ln(écart + 1))`, écart borné entre 1 et 20.
- **Variation** : `round(K * (réel - attendu))`, la même pour chaque joueur d'une équipe.

`GET /api/v1/kicker/stats/matrix-score` expose ces variations pour des écarts d'ELO de -600 à +600 et des écarts de
buts de 1 à 10.

## ELO global et saisonnier

Chaque match produit deux séries de lignes, une par joueur :

- `kicker_elo` : depuis le premier match ;
- `kicker_elo_seasonal` : depuis le début de la **saison** du match (trimestre civil de `created_at`), chaque saison
  repartant de 1500.

L'ELO courant de chaque joueur est aussi copié dans `player_kicker_informations`.

## Créer, supprimer, recalculer

- **Création** : le match est enregistré, puis l'ELO global et saisonnier sont calculés pour ce match, dans la même
  transaction ; la vue matérialisée est rafraîchie après le commit.
- **Suppression** (admin) : les lignes d'ELO postérieures à la date du match sont effacées, puis les matchs suivants
  sont recalculés dans l'ordre chronologique.
- **Recalcul complet** (admin, `POST /api/v1/kicker/matches/recalculate-elo`, et au démarrage en profil `dev`) : tout
  l'ELO est effacé puis recalculé match par match.

L'ordre des matchs est celui de `created_at`. Un match ne peut pas être inséré dans le passé.

## Visibilité de l'ELO

Un ELO n'est montré qu'à partir de `MIN_RANKED_MATCHES` (10) matchs joués :

- dans la liste des matchs, l'ELO avant match d'un joueur n'apparaît que s'il avait déjà 10 matchs (global ou dans la
  saison) ; sinon le champ est vide ;
- les classements ne listent que les joueurs qui ont au moins 10 matchs ;
- l'historique d'ELO d'un joueur (global ou d'une saison) est vide sous ce seuil.

**Chance de victoire** : calculée avec l'ELO avant match, pondéré 70 % saisonnier et 30 % global quand l'ELO
saisonnier est visible ; un ELO global non visible est remplacé par 1500.

## Statistiques

| Endpoint (`/api/v1/kicker/stats`) | Contenu                                                                               |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| `/global`                         | classement global : matchs, victoires, taux, ELO, rang, 5 derniers résultats, évolution depuis lundi |
| `/season/{year}/{quarter}`        | même chose pour une saison                                                            |
| `/season`                         | liste des saisons (matchs, joueurs), saison courante ajoutée même vide                |
| `/player/{id}`                        | fiche joueur : partenaires, adversaires, saisons, historique d'ELO                |
| `/duo`                            | statistiques par paire de partenaires                                                 |
| `/matrix-score`, `/matrix-score/results` | matrice théorique, et score du perdant selon l'écart d'ELO réel                |

## Codes d'accès

Un admin crée un code (fourni ou généré, 4 à 64 caractères alphanumériques, mis en majuscules), avec une date
d'expiration facultative. `POST /api/v1/kicker/matches/public` crée un match avec ce code : le code est verrouillé en
lecture, refusé s'il est inconnu (`401`), révoqué ou expiré (`401`), et son compteur d'usages augmente. Un code expiré
passe au statut `REVOKED` (raison `EXPIRED`) à sa prochaine lecture ; un admin peut le révoquer (raison `MANUAL`).
