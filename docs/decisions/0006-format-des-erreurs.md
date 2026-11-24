# 0006. Format des erreurs de l'API

- **Statut** : Proposé
- **Date** : 2026-11-24

## Contexte

L'API renvoie ses erreurs au format `ApiErrorResponseDTO` (`{ "message": "...", "errors": { "champ": "message" } }`),
que kicker-app lit déjà pour afficher les messages et les erreurs de validation par champ. Mais la traduction des
exceptions est incomplète : toute `RuntimeException` sort en `400` avec son message brut, y compris les refus de
droits, les doublons et les erreurs SQL, et les erreurs propres à Spring MVC (JSON illisible, paramètre manquant)
sortent sous d'autres formes.

Spring Boot 3 propose un format standard, **ProblemDetail** (RFC 9457 : `type`, `title`, `status`, `detail`,
`instance`), activable par `spring.mvc.problemdetails.enabled`.

## Options envisagées

1. **ProblemDetail** : standard, pris en charge nativement par Spring. Mais changer le format casse kicker-app sur
   tous ses écrans d'erreur, pour un gain faible tant que l'API n'a qu'un client.
2. **Garder `ApiErrorResponseDTO`** et le rendre complet et cohérent.

## Décision

Nous gardons **`ApiErrorResponseDTO`** pour toute erreur produite par l'API, avec ces règles (détail dans
[../conventions/api-design.md](../conventions/api-design.md#erreurs)) :

- exceptions nommées par domaine, traduites une à une : introuvable `404`, conflit `409`, invariant violé `400` ;
- `GlobalExceptionHandler` étend `ResponseEntityExceptionHandler` et surcharge ses méthodes pour que les erreurs de
  Spring MVC aient le même format ;
- violations de contrainte en base et conflits de version : `409`, message générique ;
- les refus de Spring Security (`AccessDeniedException`, `AuthenticationException`) ne sont jamais traduits : ils
  sont relancés pour que Spring Security réponde `401` ou `403` ;
- filet `Exception` : `500`, message générique, trace dans les logs, jamais dans la réponse.

## Conséquences

- **Positives** : aucun changement pour kicker-app ; un seul format, des statuts justes ; plus de détail interne dans
  les réponses.
- **Négatives / coûts** :
    - format maison, à documenter pour tout nouveau client ;
    - les `401` et `403` émis par les filtres de Spring Security gardent leur propre format, sauf à personnaliser
      l'`AuthenticationEntryPoint` et l'`AccessDeniedHandler`.
- **Suivi** : la mise en conformité de `GlobalExceptionHandler` est en P1 dans [../tech-debt.md](../tech-debt.md). Si
  l'API gagne d'autres clients, réévaluer ProblemDetail par un nouvel ADR.
