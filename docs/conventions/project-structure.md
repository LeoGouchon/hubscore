# Structure du projet

Où ranger chaque classe, et pourquoi on garde le nommage actuel des packages et des entités.

## Fichiers clés

- `src/main/java/com/leogouchon/hubscore/` : un package par domaine.
- `src/test/java/com/leogouchon/hubscore/` : même arborescence, plus `testutil/`.
- [../architecture/overview.md](../architecture/overview.md) : le rôle de chaque domaine aujourd'hui.

## Un package par domaine

```
com.leogouchon.hubscore
├── common/                    transverse : config/, dto/ (pagination, erreur)
├── user_service/              comptes, rôles, invitations
├── player_service/            joueurs, teams
└── kicker_match_service/      matchs, ELO, statistiques, codes d'accès
    ├── controller/
    ├── dto/                   (dto/controller_params/ pour les paramètres de requête complexes)
    ├── entity/
    ├── exception/
    ├── repository/
    │   └── projection/
    ├── service/               interfaces
    │   └── impl/              implémentations XxxServiceDefault
    └── specification/
```

- Un nouveau concept métier va dans le domaine qui possède ses données. Un nouveau domaine (un autre sport, par
  exemple) reçoit son propre package `<domaine>_service`, avec les mêmes sous-dossiers.
- Ce qui sert à plusieurs domaines sans appartenir à l'un d'eux va dans `common/`.
- Un domaine en appelle un autre **par son service** (`PlayerService`), pas par son repository. Les entités d'un
  autre domaine peuvent être référencées par une relation JPA.

**État** : ⚠️ `IdentityProvisioningController` et `IdentityUserSynchronizer` utilisent `PlayerRepository`.

## Les couches

| Couche              | Responsabilité                                                    | Ne fait pas                                     |
| ------------------- | ----------------------------------------------------------------- | ----------------------------------------------- |
| `controller/`       | HTTP : chemin, paramètres, validation, droits, statut, DTO         | de logique métier, d'accès au repository        |
| `service/`          | interface : le contrat métier, en types du domaine ou DTO          |                                                 |
| `service/impl/`     | logique, règles, transactions, appels aux repositories            | de HTTP (`ResponseEntity`, `HttpServletRequest`) |
| `repository/`       | accès aux données (Spring Data)                                   | de logique métier                               |
| `entity/`           | état persistant et invariants simples (constructeur, `isActive()`) | d'appel à un service                            |
| `dto/`              | formes d'entrée et de sortie de l'API                              | d'accès aux données                             |

**Toujours une interface pour un service** : `XxxService` dans `service/`, `XxxServiceDefault` dans `service/impl/`,
même avec une seule implémentation. C'est ce qui permet aujourd'hui deux implémentations d'ELO interchangeables et des
tests qui simulent un service par son contrat.

**État** : ⚠️ `InvitationService` et `IdentityUserSynchronizer` sont des classes sans interface ; `MeController`,
`RoleController` et `IdentityProvisioningController` appellent `UserRepository` directement ; des services lèvent des
`ResponseStatusException` (HTTP dans le service).

## Plusieurs implémentations

Quand une interface a plusieurs implémentations (ELO global et ELO saisonnier pour `KickerEloService`) :

- chaque implémentation est nommée par sa variante, `<Variante><Concept>Service` (`GlobalKickerEloService`,
  `SeasonalKickerEloService`), et porte un `@Qualifier` ;
- **on en choisit une** (vrai pattern strategy) : l'appelant injecte celle qu'il veut par son qualifier ;
- **on les appelle toutes** à chaque fois : une **façade** (`KickerEloRecalculationService`, interface + `Default`)
  injecte `List<KickerEloService>` et fait l'enchaînement complet (dont le rafraîchissement de la vue). Les appelants
  ne connaissent que la façade : une variante ajoutée demain ne demande de modifier aucun appelant.

**État** : ⚠️ l'ELO est dans le second cas mais sans façade : `KickerMatchServiceDefault` appelle les deux qualifiers et
le rafraîchissement dans chaque écriture.

## Fichiers satellites

- **Mapper** : la conversion entité → DTO va dans une méthode statique `from(entity)` du `record` de réponse, ou dans
  une classe `<Domaine>Mapper` (`utils/UsersMapper`) quand elle concerne plusieurs DTO.
- **Projection** : une interface par requête native, dans `repository/projection/`, nommée
  `<Contenu>Projection`.
- **Specification** : une classe `<Entité>Specifications` à méthodes statiques, dans `specification/`.
- **Règles constantes** : une classe `final` à constantes (`KickerEloVisibilityRules`) plutôt que des nombres
  magiques.

## Nommage conservé

Deux choix s'écartent des conventions Java courantes ; **on les garde** pour rester cohérent, et le nouveau code les
suit :

- **Packages de domaine en snake_case** avec le suffixe `_service` (`kicker_match_service`, `controller_params`).
- **Entités au pluriel** (`Players`, `Users`, `Teams`, `KickerMatches`), comme les tables.

Renommer imposerait une grosse PR de déplacement pour peu de gain. L'écart est noté dans
[../tech-debt.md](../tech-debt.md) au cas où un renommage serait décidé un jour (dans une PR dédiée, avec un ADR).

## Tests

`src/test/java` reproduit l'arborescence de `src/main/java` (`kicker_match_service/service/...Tests.java`). Les
fabriques de données de test partagées vont dans `testutil/`. Détail : [testing.md](testing.md).

## Ressources

- `src/main/resources/db/migration/` : migrations Flyway ([persistence.md](persistence.md)).
- `src/main/resources/db/dev-data/` : données de dev, jamais chargées en production.
- Aucun fichier `application*.yml` dans `src/main/resources` : la configuration est externe (`config/`).
