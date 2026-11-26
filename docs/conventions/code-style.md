# Style de code

Les règles d'écriture Java communes à tout le projet. Il n'y a pas encore de formateur automatique (voir
[../tech-debt.md](../tech-debt.md)) : on suit le style du code voisin, formaté par l'IDE (indentation de 4 espaces).

## Fichiers clés

- `pom.xml` : Java 21, Lombok.
- `kicker_match_service/service/impl/KickerMatchAccessCodeServiceDefault.java` : un service typique.

## Injection par constructeur

Les dépendances sont des champs `private final`, injectés par l'unique constructeur. Pas d'`@Autowired` (inutile avec
un seul constructeur), jamais sur un champ. Un bean est injecté par son **interface**, jamais par sa classe concrète,
et jamais instancié avec `new` dans un autre bean.

```java
@Service
public class KickerMatchAccessCodeServiceDefault implements KickerMatchAccessCodeService {
    private final KickerMatchAccessCodeRepository accessCodeRepository;

    public KickerMatchAccessCodeServiceDefault(KickerMatchAccessCodeRepository accessCodeRepository) {
        this.accessCodeRepository = accessCodeRepository;
    }
}
```

Plusieurs implémentations d'une même interface : voir [project-structure.md](project-structure.md#plusieurs-implémentations).

**État** : ⚠️ `@Autowired` redondant sur de nombreux constructeurs ; `@Value` sur des champs dans `SecurityConfig` ;
`EloMatrixServiceDefault` injecte `EloCalculatorDefault` et `SeasonalKickerEloService` l'instancie avec `new`.

## Configuration

Une propriété de `config/application.yml` est lue par un `record` `@ConfigurationProperties`, validé au démarrage,
puis injecté là où il sert :

```java
@Validated
@ConfigurationProperties(prefix = "identity")
public record IdentityProperties(@NotBlank String issuer, @NotBlank String audience, @NotBlank String jwkSetUri) {
}
```

- `@ConfigurationPropertiesScan` sur `HubscoreApplication` enregistre toutes ces classes ; sans lui, une nouvelle
  classe n'est pas liée.
- Pas de `@Value` éparpillés : toute la configuration d'un domaine est au même endroit, et une valeur manquante
  arrête le démarrage avec un message clair.

**État** : ⚠️ seule `CorsProperties` existe (classe avec setters, enregistrée par `@EnableConfigurationProperties`) ;
les propriétés `identity.*` sont lues par `@Value` dans trois classes.

## Lombok

- Sur une **entité** : `@Getter` seulement. Pas de `@Setter` de classe (il contourne les invariants, voir
  [persistence.md](persistence.md#entités)), ni `@Data`, `@EqualsAndHashCode` ou `@ToString` (relations chargées en
  cascade, récursion). `equals` / `hashCode` s'écrivent à la main pour une clé composite (`KickerEloId`).
- Ailleurs : `@Slf4j` pour les logs. Les DTO et la configuration sont des `record` : pas de Lombok.

**État** : ⚠️ `@Getter @Setter` de classe sur toutes les entités et la plupart des DTO.

## Nommage

| Élément              | Règle                                                | Exemple                                    |
| -------------------- | ---------------------------------------------------- | ------------------------------------------ |
| Classe, record       | `PascalCase`, nom de rôle en suffixe                 | `KickerMatchController`, `PlayerRequestDTO` |
| Interface de service | `<Concept>Service`                                   | `KickerStatService`                        |
| Implémentation       | `<Concept>ServiceDefault` dans `service/impl/`       | `KickerStatServiceDefault`                 |
| Variante             | `<Variante><Concept>Service` (plusieurs implémentations) | `GlobalKickerEloService`               |
| DTO                  | suffixe `RequestDTO` / `ResponseDTO` (ou `DTO`)      | `KickerMatchResponseDTO`                   |
| Exception métier     | `<Entité>NotFoundException`, `<Cause>Exception`      | `InvalidKickerMatchAccessCodeException`    |
| Méthode, variable    | `camelCase`, verbe pour une méthode                  | `getActiveCodeForUse`                      |
| Constante            | `UPPER_SNAKE_CASE`, `static final`                   | `MIN_RANKED_MATCHES`                       |

Packages et entités : [project-structure.md](project-structure.md). Tests : [testing.md](testing.md#nommage).

## Imports et types

- Toujours des imports, jamais de nom pleinement qualifié dans le code (`java.util.UUID.fromString(...)`).
- `var` est accepté quand le type est évident à droite du `=`.
- `Optional` en retour d'une recherche qui peut ne rien trouver ; jamais en paramètre ni en champ. Pas de `.get()` :
  `orElseThrow` avec l'exception métier.

**État** : ⚠️ noms pleinement qualifiés dans `SecurityConfig` et `MeController` ; `.get()` dans `getDuoStats`.

## Le temps

- `java.time` uniquement (`Instant`, `LocalDate`, `ZoneId`) : ni `Calendar`, ni `java.util.Date`, ni
  `java.sql.Timestamp` dans un nouveau code.
- Le fuseau métier est `Europe/Paris`, défini **une seule fois** (constante partagée dans `common/`) et passé
  explicitement ; on ne dépend jamais du fuseau de la JVM (UTC dans Docker).
- « Maintenant » vient d'un `Clock` injecté (bean `Clock.systemUTC()`), jamais de `Instant.now()` ou
  `LocalDate.now()` sans argument : les tests figent le temps avec `Clock.fixed(...)`.

**État** : ⚠️ `Calendar`, `toLocalDateTime()` et `LocalDate.now()` dans les statistiques et l'ELO saisonnier ; le
fuseau n'est défini que dans `KickerMatchSpecifications` ; le `Clock` (`common/config/ClockConfig`) n'est utilisé que
par `KickerStatServiceDefault.getGlobalStats`.

## Exceptions

- Une erreur métier lève une exception nommée du domaine (`<domaine>/exception/`), ou une `IllegalArgumentException`
  pour un invariant d'entité violé. Traduction en statut : [api-design.md](api-design.md#erreurs).
- On n'attrape une exception que pour la traiter. Pas de `catch (Exception e) { throw new RuntimeException(...) }`.
- Pas de `throws RuntimeException` dans une signature.

**État** : ⚠️ `UserServiceDefault.deleteUser`, `PlayerServiceDefault.createPlayer`, `MeController` enveloppent leurs
erreurs ; `EntityNotFoundException` et `RuntimeException("Player not found")` au lieu d'exceptions nommées.

## Logs et commentaires

- Logs avec `@Slf4j` (`log.info`, `log.warn`, `log.error` avec l'exception), jamais `System.out`. Pas de donnée
  personnelle (e-mail) ni de secret dans un log.
- Un commentaire dit le _pourquoi_ que le code ne montre pas, en une ligne, en anglais. Pas de code commenté.
- Javadoc pour une méthode publique dont le comportement ne se lit pas dans son nom (comme dans
  `KickerMatchSpecifications`).

## Langue

Code, commentaires, messages d'erreur renvoyés par l'API et commits en **anglais**. Documentation (`docs/`, `specs/`)
en **français**.
