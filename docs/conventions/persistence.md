# Persistance

Règles pour les entités, les requêtes, les migrations et les écritures. Le fonctionnement actuel est décrit dans
[../architecture/persistence.md](../architecture/persistence.md).

## Fichiers clés

- `src/main/resources/db/migration/` : migrations Flyway ; `src/main/resources/db/dev-data/` : données de dev.
- `kicker_match_service/entity/KickerMatches.java` : invariants posés dans le constructeur.
- `kicker_match_service/repository/KickerMatchAccessCodeRepository.java` : verrou pessimiste.

## Migrations Flyway

- **Tout changement de schéma** passe par une nouvelle migration `V<n+1>__<description_snake_case>.sql`, `n` étant le
  plus grand numéro de `origin/dev` à jour. Recette : [../process/add-a-migration.md](../process/add-a-migration.md).
- **On ne modifie jamais une migration mergée** : Flyway refuse de démarrer si sa somme de contrôle change. On corrige
  par une nouvelle migration.
- Une migration doit passer sur les **données de production** : colonne `NOT NULL` ajoutée nullable, remplie, puis
  contrainte ; suppression de données décidée dans le brief.
- Les contraintes vivent en base : `NOT NULL`, `UNIQUE`, clés étrangères, `CHECK`. Noms explicites
  (`fk_<table>_<colonne>`, `idx_<table>_<colonnes>`, `chk_<table>_<colonne>`).
- **Données de dev** : une migration **répétable** `R__seed_dev_data.sql` (idempotente : `ON CONFLICT DO NOTHING`),
  jamais une migration versionnée. Une `V9999` bloquerait toute migration plus récente sur une base locale.

**État** : ⚠️ le seed est `V9999__seed_dev_data.sql` : une base locale qui l'a chargé doit être reconstruite
(`make rebuild`) à chaque nouvelle migration.

## Entités

Une entité garde ses règles : on ne la met pas dans un état invalide de l'extérieur.

- **Pas de `@Setter` de classe.** Une modification passe par une méthode qui dit l'intention et vérifie la règle :
  `player.rename(firstname, lastname)`, `accessCode.revoke(reason)`, `accessCode.markUsed()`. Un setter n'existe que
  pour un champ sans règle, posé au cas par cas.
- Un constructeur sans argument `protected` pour JPA, et un constructeur public qui pose les invariants
  (`KickerMatches` vérifie 1 contre 1 ou 2 contre 2, des joueurs distincts et un score de 10).
- Nom au pluriel comme la table ([project-structure.md](project-structure.md)), `@Table(name = ...)` explicite,
  `@Column(name = ...)` quand le nom Java diffère.
- Identifiant `UUID` généré (`@GeneratedValue`), `updatable = false`.
- Enums stockés en texte (`@Enumerated(EnumType.STRING)`), jamais en ordinal, avec un `CHECK` en base.
- Instants : champ `Instant` sur une colonne `TIMESTAMP WITH TIME ZONE` (valeur absolue, quel que soit le fuseau du
  serveur) ; jour sans heure : `LocalDate` sur `DATE`. Valeur par défaut posée par le code (`Clock`) ou par la base,
  pas les deux.
- **Relations** : `@ManyToOne(fetch = FetchType.LAZY)` et `@OneToOne(fetch = FetchType.LAZY)` **toujours** (le défaut
  JPA est `EAGER`). Une relation lue par un cas d'usage se charge explicitement (`JOIN FETCH`, `@EntityGraph`).
- Pas de cascade de suppression implicite : les suppressions en cascade sont des clés étrangères
  `ON DELETE CASCADE` posées par migration.
- `@Version` sur une entité modifiée en concurrence (`PlayerKickerInformations`).
- Pas d'annotation Jackson sur une entité : elle ne sort jamais dans l'API.

**État** : ⚠️ `@Setter` de classe sur toutes les entités (`accessCode.setStatus(ACTIVE)` contourne `revoke()`,
`match.setPlayer2A(null)` casse la règle 1 contre 1 / 2 contre 2) ; 13 relations `@ManyToOne` / `@OneToOne` en `EAGER`
(5 sur `KickerMatches`) ; annotations Jackson sur des entités ; constructeurs publics sans argument.

## Requêtes

Choisir, dans l'ordre, la première option qui suffit :

1. une **requête dérivée** Spring Data (`findByIdentityUserId`) ;
2. du **JPQL** avec `@Query` et des paramètres nommés (`@Param`) ;
3. une **Specification** pour un filtre dynamique (combinaison de critères optionnels) ;
4. du **SQL natif** pour une agrégation que JPQL ne sait pas écrire (statistiques, fenêtres). Le résultat est lu par
   une interface de **projection** de `repository/projection/`, pas par un DTO de l'API.

Toujours des paramètres liés, jamais de concaténation de chaîne. Une requête lancée pour chaque élément d'une liste
(N+1) se remplace par une requête groupée (`findAllByMatchIdIn`).

**État** : ⚠️ requêtes natives mappées sur des DTO (`PartnerStatsDTO`, `EloHistoryDTO`…) ; requêtes dans des boucles
dans `getGlobalStats`, `getSeasonStats` et `getDuoStats`.

## Transactions et chargement

- `spring.jpa.open-in-view: false` : la session JPA se ferme avec la transaction du service. Le service renvoie donc
  des DTO (ou des entités déjà chargées), et la conversion `from(entity)` se fait **dans** le service.
- `@Transactional` de **Spring** (`org.springframework.transaction.annotation`), sur les méthodes publiques de service,
  jamais sur un controller ; `@Transactional(readOnly = true)` sur les lectures.
- Une méthode qui enchaîne plusieurs écritures (match, ELO global, ELO saisonnier) est **une seule** transaction.
  `Propagation.REQUIRES_NEW` seulement avec une raison écrite en commentaire.
- Un compteur ou une ressource lue puis écrite se verrouille (`@Lock(PESSIMISTIC_WRITE)`) ou se protège par
  `@Version` ; une contrainte `UNIQUE` reste le dernier rempart contre un doublon concurrent (erreur traduite en `409`,
  [api-design.md](api-design.md#erreurs)).

**État** : ⚠️ `open-in-view` non configuré (actif par défaut) ; aucune lecture en `readOnly` ;
`PlayerServiceDefault` utilise `jakarta.transaction.Transactional` ; le recalcul complet de l'ELO enchaîne deux
transactions `REQUIRES_NEW` sans transaction englobante (un échec du saisonnier laisse le global recalculé).

## Données dérivées

L'ELO (`kicker_elo`, `kicker_elo_seasonal`, `player_kicker_informations`) et la vue `mv_player_match_facts` dérivent
des matchs. Toute écriture sur `kicker_matches` passe par **une façade unique** (cible :
`KickerEloRecalculationService`) qui met à jour l'ELO global, l'ELO saisonnier, puis rafraîchit la vue après le commit.
Un appelant n'enchaîne pas lui-même les deux implémentations ni le rafraîchissement
([project-structure.md](project-structure.md#plusieurs-implémentations)).

**État** : ⚠️ `KickerMatchServiceDefault` appelle les deux qualifiers et le rafraîchissement dans chaque méthode
d'écriture. Détail du calcul : [../architecture/elo-and-stats.md](../architecture/elo-and-stats.md).
