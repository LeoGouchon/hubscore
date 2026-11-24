# 0004. Organisation par domaine et en couches

- **Statut** : Proposé
- **Date** : 2026-11-24

## Contexte

Le code est déjà découpé en packages de domaine (`user_service`, `player_service`, `kicker_match_service`) avec des
couches `controller` / `service` / `repository`, et la plupart des services ont une interface et une implémentation
`XxxServiceDefault`. Mais les exceptions se multiplient : controllers qui appellent un repository, services sans
interface, entités renvoyées dans les réponses, DTO écrits de trois façons. Il faut fixer la cible avant d'écrire la
suite.

## Options envisagées

1. **Services en classe seule** tant qu'il n'y a qu'une implémentation (recommandation courante de Spring) : moins de
   fichiers, mais deux styles cohabiteraient avec l'existant.
2. **Toujours une interface + `Default`** : cohérent avec la majorité du code, permet plusieurs implémentations (ELO
   global et saisonnier) et des tests par le contrat.
3. **Renommer** packages (sans underscore) et entités (au singulier) pour suivre les conventions Java, ou **garder**
   le nommage actuel.

## Décision

- **Couches** : controller → service → repository ; un controller n'appelle jamais un repository.
- **Services** : toujours une interface `XxxService` et son implémentation `service/impl/XxxServiceDefault`.
- **DTO** : les nouveaux DTO sont des `record` Java validés ; aucune entité JPA dans une requête ou une réponse.
- **Entités riches** : pas de `@Setter` de classe ; une entité garde ses invariants et se modifie par des méthodes
  d'intention (`rename`, `revoke`). Un modèle anémique laisserait n'importe quel service la mettre dans un état
  invalide.
- **Plusieurs implémentations** : nommées `<Variante><Concept>Service`. Si on en choisit une, c'est une strategy
  (sélection par `@Qualifier`) ; si on les appelle toutes (ELO global et saisonnier), une façade les enchaîne
  (`KickerEloRecalculationService`, qui injecte `List<KickerEloService>`) et les appelants ne connaissent qu'elle.
- **Nommage conservé** : packages `<domaine>_service` en snake_case et entités au pluriel. Un renommage coûterait une
  grosse PR et des conflits avec toutes les branches ouvertes, pour un gain de lisibilité faible.
- **Tests** : chaque service et chaque endpoint créé ou modifié a son test (voir
  [../conventions/testing.md](../conventions/testing.md)).

## Conséquences

- **Positives** : une seule façon d'écrire un endpoint ; contrat d'API découplé du schéma.
- **Négatives / coûts** : un fichier de plus par service ; les DTO Lombok, les entités exposées, les setters
  d'entité et les appels directs aux deux services d'ELO restent à migrer au fil des PR.
- **Suivi** : écarts listés dans [../tech-debt.md](../tech-debt.md) (P2), dont le nommage non standard, au cas où un
  renommage serait décidé plus tard par un nouvel ADR.
