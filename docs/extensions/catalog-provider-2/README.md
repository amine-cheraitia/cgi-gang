# Extension: Second Catalog Provider

## Objectif

Ajouter un second provider catalogue (mock d'abord, provider reel ensuite) pour prouver l'interchangeabilite des adapters.

## SOLID et Patterns

- **DIP**: les use cases dependent du port `CatalogProvider`.
- **OCP**: ajout d'un provider sans modifier la couche application.
- **Patterns**: `Adapter` (provider #2), `Facade` optionnelle pour failover.

## Impact architecture

- **Domain/Application**: inchange.
- **Infrastructure**: nouvel adapter `SecondCatalogProviderAdapter`.
- **Configuration**: choix provider via propriete (`catalog.provider=mock|provider2`).

## Tests cibles

- TU adapter provider #2.
- TI endpoint `/api/events/*` avec provider #2 actif.
- TI indisponibilite provider avec code erreur `CAT-002`.
