# Tests

Ce qui doit être testé, et comment écrire les tests. La suite tourne avec `./mvnw -B test`, en local comme en CI.

## Fichiers clés

- `src/test/java/com/leogouchon/hubscore/` : tests, même arborescence que `src/main/java`.
- `src/test/java/com/leogouchon/hubscore/testutil/` : fabriques de données (`TestPlayers`, `TestKickerMatches`,
  `TestKickerMatchRequests`).
- `src/test/resources/application-test.yml` : contexte Spring de test (H2, Flyway désactivé), chargé par le profil
  `test`.
- `pom.xml` : `spring-boot-starter-test` (JUnit 5, Mockito, AssertJ, MockMvc) et `spring-security-test` (`jwt()`).
- `.github/workflows/tests.yml` : CI sur chaque PR et push vers `dev` et `main`.

**État** : ⚠️ `spring-security-test` n'est pas encore dans `pom.xml` : à ajouter (scope `test`) avec le premier test de
droits.

## La règle

Tout code nouveau ou modifié est testé dans la même tâche que lui, avant ou après le code :

- **chaque service ou règle métier** créé ou modifié a un **test unitaire** de son implémentation ;
- **chaque méthode d'entité qui porte un invariant** (`rename`, `revoke`) a son test unitaire, sans Spring ;
- **chaque endpoint** créé ou modifié a un **test MockMvc** : cas nominal (statut et corps), validation (`400`),
  ressource absente (`404`) si elle existe, et chaque règle de droit du brief (`401`, `403` ou accès autorisé) ;
- **chaque correctif** commence par un test qui reproduit le bug.

Un bon test échoue quand la règle casse. On vise les comportements du brief, pas un pourcentage de couverture
(JaCoCo donne un repère, pas un objectif).

## Tests unitaires de service

Sans Spring : on construit l'implémentation avec des dépendances simulées par Mockito.

```java
class KickerMatchAccessCodeServiceDefaultTests {
    private KickerMatchAccessCodeRepository repository;
    private KickerMatchAccessCodeServiceDefault service;

    @BeforeEach
    void setUp() {
        repository = mock(KickerMatchAccessCodeRepository.class);
        service = new KickerMatchAccessCodeServiceDefault(repository);
    }

    @Test
    void should_reject_duplicate_custom_code() {
        when(repository.existsByCode("DUPLICATE")).thenReturn(true);

        assertThatThrownBy(() -> service.createCode("duplicate", 8, null, admin))
                .hasMessageContaining("Access code already exists");
        verify(repository, never()).save(any());
    }
}
```

- Assertions **AssertJ** (`assertThat`, `assertThatThrownBy`), pas celles de JUnit.
- `ArgumentCaptor` pour vérifier ce qui est enregistré.
- **Le temps** : le service reçoit un `Clock` ([code-style.md](code-style.md#le-temps)) ; le test lui passe
  `Clock.fixed(...)` pour tester les frontières (changement de saison, expiration).

**État** : ⚠️ `KickerStatServiceDefault` isole le temps par une méthode surchargeable (`currentDate()`), qui oblige à
sous-classer le service dans le test.

## Tests de controller

`@WebMvcTest(<Controller>.class)` avec le service simulé par `@MockitoBean`, et **toujours** le profil de test :

```java
@WebMvcTest(KickerMatchController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class KickerMatchControllerTests {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private KickerMatchService matchService;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private IdentityUserSynchronizer identityUserSynchronizer;
}
```

- **Droits** : un test qui vérifie un `401` ou un `403` garde les filtres de sécurité (pas de
  `@AutoConfigureMockMvc(addFilters = false)`). `jwt()` de `spring-security-test` remplace **le décodeur et le
  convertisseur** de `SecurityConfig` : les autorités se donnent dans le test
  (`jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))`).
- On vérifie le statut, les champs utiles du JSON (`jsonPath`) et l'appel au service avec les bons arguments.

**État** : ⚠️ un seul test de controller (`KickerMatchControllerTests`), filtres désactivés ; aucun test de droit.
Un refus de `@PreAuthorize` sort aujourd'hui en `400` ([api-design.md](api-design.md#erreurs)) : le premier test de
droit échouera tant que `GlobalExceptionHandler` n'est pas corrigé.

## Tests de requêtes

- `@DataJpaTest` + `@ActiveProfiles("test")` pour une requête dérivée, JPQL ou une Specification, sur H2.
- **Limite actuelle** : H2 ne joue ni les migrations ni la vue matérialisée, et ne comprend pas tout le SQL
  PostgreSQL. Une requête native se vérifie donc à la main sur `make dev` pendant la vérification, et le rapport le
  dit (Testcontainers est prévu dans [../tech-debt.md](../tech-debt.md)).

**État** : ⚠️ `KickerMatchSpecificationsTest` passe par `@TestPropertySource` au lieu du profil `test`.

## Profil de test

`src/main/resources` ne contient aucun `application.yml` : sans `@ActiveProfiles("test")`, un test de couche
(`@WebMvcTest`, `@DataJpaTest`) ou d'intégration (`@SpringBootTest`) démarre sans configuration (Flyway sur H2,
propriétés `identity.*` absentes). Le profil est donc obligatoire sur chacun.

## Données de test

- Les objets métier se construisent avec les fabriques de `testutil/` (`TestPlayers.alice(id)`,
  `TestKickerMatches.singlesMatch(...)`). Une fabrique utile à plusieurs tests y est ajoutée plutôt que dupliquée.
- Des valeurs lisibles et celles du brief, pas des valeurs aléatoires qui masquent l'intention.

## Nommage

- Classe : `<ClasseTestée>Tests` (au pluriel), dans le même package que la classe testée, fichier du même nom.
- Méthode : `should_<comportement>[_when_<condition>]`, en anglais, par exemple
  `should_create_public_match_with_access_code`.
- Un test vérifie un comportement ; plusieurs `assertThat` sont permis s'ils décrivent le même résultat.

**État** : ⚠️ `KickerMatchSpecificationsTest` ; `EloCalculatorDefaultTests.java` contient la classe
`EloCalculatorDefaultTest`.
