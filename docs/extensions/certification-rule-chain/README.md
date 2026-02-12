# Extension: Certification Rule Chain

## Objectif

Rendre la certification d'annonce extensible via une chaine de regles activables/desactivables.

## SOLID et Patterns

- **SRP**: une regle = une responsabilite de validation.
- **OCP**: ajout de nouvelles regles sans modifier le use case principal.
- **ISP**: interface simple `CertificationRule`.
- **Patterns**: `Chain of Responsibility` pour evaluation ordonnee.

## Impact architecture

- **Domain**: `CertificationRule`, `CertificationContext`, resultats de validation.
- **Application**: orchestration de chaine avant `CERTIFIED`.
- **Infrastructure**: regles branchees via configuration Spring.

## Tests cibles

- TU par regle (normal, anormal, limite).
- TU de la chaine (ordre, short-circuit, messages).
- TI endpoint certification avec scenarios success/failure.
