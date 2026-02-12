# Extension: Dynamic Pricing Policies

## Objectif

Permettre des politiques de frais/payout variables selon le contexte (type d'evenement, fenetre temporelle, campagne).

## SOLID et Patterns

- **OCP**: nouvelles politiques ajoutees sans modifier `PlaceOrderUseCase`.
- **SRP**: calcul pricing isole dans des policies dediees.
- **LSP**: toutes les policies respectent le contrat `PricingPolicy`.
- **Patterns**: `Strategy` (policy), `Resolver` applicatif (selection policy active).

## Impact architecture

- **Domain**: introduction de `PricingPolicy` et conservation de `PricingBreakdown`.
- **Application**: `PricingPolicyResolver` injecte dans le use case de commande.
- **Infrastructure**: mapping configuration -> policy active.

## Tests cibles

- TU par policy (`Standard`, `PremiumEvent`, `LastMinute`).
- TU resolver (priorite et fallback).
- TI API orders: verification des montants selon policy configuree.
