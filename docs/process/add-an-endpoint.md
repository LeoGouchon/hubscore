# Ajouter un endpoint

Créer ou modifier une route de l'API, de l'entité jusqu'à la doc. Les règles sont dans
[../conventions/api-design.md](../conventions/api-design.md) ; cette recette donne l'ordre et un squelette.

## Fichiers clés

- `<domaine>/entity/`, `<domaine>/service/`, `<domaine>/service/impl/`, `<domaine>/dto/`, `<domaine>/controller/`,
  `<domaine>/exception/`.
- `common/config/SecurityConfig.java` : routes publiques.
- `common/config/GlobalExceptionHandler.java` : traduction des exceptions.
- [../architecture/api.md](../architecture/api.md) : la liste des endpoints à tenir à jour.

## Checklist

1. [ ] **Contrat** écrit (dans le brief) : chemin, méthode, paramètres, corps, réponse, erreurs, droits.
2. [ ] **Entité** : la règle métier qui ne dépend que d'elle est une méthode de l'entité, avec son test unitaire.
3. [ ] **Service** : méthode ajoutée à l'interface `XxxService`, implémentée dans `XxxServiceDefault`
       (`@Transactional`, `readOnly = true` pour une lecture), exceptions nommées pour les erreurs, conversion en DTO
       dans le service.
4. [ ] **Test du service** : un test par règle et par cas d'erreur.
5. [ ] **DTO** `record` de requête (validé, `@Valid` sur les objets imbriqués) et de réponse (`from(entity)`).
6. [ ] **Controller** : route, `@Valid @RequestBody`, statut, `@Operation` / `@ApiResponse`. Pas de `@Validated`.
7. [ ] **Droits** : `@PreAuthorize` pour un rôle (attention : un `GET` sous `/api/v1/kicker/**` ou
       `/api/v1/players/**` est public par défaut) ; droit lié à la ressource vérifié dans le service ; route publique
       ajoutée explicitement dans `SecurityConfig` et marquée `@SecurityRequirements()` pour OpenAPI.
8. [ ] **Erreur nouvelle** : exception nommée et sa traduction dans `GlobalExceptionHandler`.
9. [ ] **Test MockMvc** : nominal, validation, `404` le cas échéant, droits (`401`, `403`).
10. [ ] **Doc** : ligne dans [../architecture/api.md](../architecture/api.md) (et
        [../architecture/security.md](../architecture/security.md) si une route devient publique).
11. [ ] **Front** : si le contrat d'un endpoint existant change, vérifier l'usage dans kicker-app et prévoir la
        compatibilité ([api-design.md](../conventions/api-design.md#compatibilité-avec-le-front)).

## Squelette

Entité (la règle vit ici) :

```java
public void rename(String firstname, String lastname) {
    if (firstname == null || firstname.isBlank() || lastname == null || lastname.isBlank()) {
        throw new IllegalArgumentException("Firstname and lastname are required");
    }
    this.firstname = firstname.trim();
    this.lastname = lastname.trim();
}
```

Service :

```java
public interface PlayerService {
    PlayerSummaryResponseDTO renamePlayer(UUID id, PlayerRenameRequestDTO request);
}

@Service
public class PlayerServiceDefault implements PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerServiceDefault(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    @Transactional
    public PlayerSummaryResponseDTO renamePlayer(UUID id, PlayerRenameRequestDTO request) {
        Players player = playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));
        player.rename(request.firstname(), request.lastname());
        return PlayerSummaryResponseDTO.from(player);
    }
}
```

DTO :

```java
public record PlayerRenameRequestDTO(
        @NotBlank @Size(max = 100) String firstname,
        @NotBlank @Size(max = 100) String lastname
) {
}
```

Controller :

```java
@PatchMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
@Operation(summary = "Rename a player")
@ApiResponse(responseCode = "200", description = "Player renamed")
@ApiResponse(responseCode = "404", description = "Player not found", content = @Content(schema = @Schema()))
public ResponseEntity<PlayerSummaryResponseDTO> renamePlayer(@PathVariable UUID id,
                                                             @Valid @RequestBody PlayerRenameRequestDTO request) {
    return ResponseEntity.ok(playerService.renamePlayer(id, request));
}
```

Test MockMvc, avec les filtres de sécurité actifs (en-tête de classe : [testing.md](../conventions/testing.md#tests-de-controller)) :

```java
@Test
void should_forbid_rename_when_user_is_not_moderator() throws Exception {
    mockMvc.perform(patch("/api/v1/players/{id}", playerId)
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"firstname\":\"Alice\",\"lastname\":\"A\"}"))
            .andExpect(status().isForbidden());
    verifyNoInteractions(playerService);
}
```

`jwt()` vient de `spring-security-test` : il remplace le décodeur **et** le convertisseur de `SecurityConfig`, donc
le rôle se donne dans le test.

> Aucun test de droits n'existe encore : ce squelette est la cible. Il demande d'ajouter `spring-security-test` au
> `pom.xml`, et il échouera (`400` au lieu de `403`) tant que `GlobalExceptionHandler` attrape les refus de droits
> ([../tech-debt.md](../tech-debt.md)). Le premier test de ce type confirme la mise en place et corrige cette recette
> si besoin.
