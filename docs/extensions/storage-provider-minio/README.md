# Extension: Storage Provider MinIO

## Objectif

Ajouter MinIO comme provider objet supplementaire en gardant l'interface `ObjectStorage` inchangée.

## SOLID et Patterns

- **DIP**: `ObjectStorage` reste le contrat stable.
- **OCP**: ajout d'un provider sans toucher aux use cases upload/presign.
- **Patterns**: `Adapter` (MinIO), `Factory/Config` (selection provider).

## Impact architecture

- **Domain/Application**: aucun changement fonctionnel.
- **Infrastructure**: `MinioObjectStorageAdapter` + config credentials/endpoint.
- **Operabilite**: environnement local de demo proche S3.

## Tests cibles

- TU adapter MinIO (put/get url/presign).
- TI upload local provider=minio.
- TI fallback comportement si presign indisponible (`LST-005`).
