# Scénarios de test et taux de couverture — Ticketio

Document de soutenance : inventaire des scénarios de test et exigence de couverture de code.

---

## 1. Exigence de couverture (barème)

Le projet impose un **minimum de 30 % de couverture de lignes** (JaCoCo) sur l’ensemble du code. Cette règle est :

- **Enforced** au build : `mvn verify` échoue si la couverture est inférieure au seuil.
- **Vérifiée en CI** : le pipeline GitHub Actions exécute `mvn clean verify`, donc la couverture est contrôlée à chaque push/PR.
- **Configurée** dans le `pom.xml` parent : `coverage.minimum=0.30`, plugin `jacoco-maven-plugin` avec une règle `LINE` / `COVEREDRATIO` ≥ 30 %.

**Comment consulter le taux de couverture :**

```bash
mvn clean verify
```

Les rapports JaCoCo sont générés dans chaque module sous :

- `marketplace-domain/target/site/jacoco/index.html`
- `marketplace-application/target/site/jacoco/index.html`
- `marketplace-infrastructure/target/site/jacoco/index.html`

Ouvrir ces fichiers dans un navigateur pour voir le pourcentage par package/classe et les lignes couvertes ou non.

---

## 2. Types de tests et emplacement

| Type | Outil | Emplacement | Rôle |
|------|--------|-------------|------|
| **Tests unitaires domaine** | JUnit 5, AssertJ | `marketplace-domain/src/test/` | Entités, VOs, services domaine, invariants métier |
| **Tests unitaires application** | JUnit 5, Mockito, AssertJ | `marketplace-application/src/test/` | Use cases, handlers, templates, dispatcher |
| **Tests d’intégration REST** | Spring Boot Test, MockMvc | `marketplace-infrastructure/src/test/` | Controllers, sécurité, erreurs API, flux E2E |
| **Tests de mutation** | PIT (Pitest) | Profil Maven `mutation` | Qualité des TU (mutations tuées) |

---

## 3. Scénarios de test par domaine

### 3.1 Domaine (`marketplace-domain`)

| Fichier de test | Scénarios couverts |
|-----------------|--------------------|
| `MoneyTest` | Création, validation (montant négatif, devise vide), égalité, conversion. |
| `PricingBreakdownTest` | Calcul commission plateforme, montant vendeur, montant acheteur, arrondis. |
| `ListingTest` | Création (`create`), rehydrate, statut initial PENDING_CERTIFICATION, validation (eventId/sellerId/price). |
| `OrderTest` | Création (`place`), rehydrate, statuts (PENDING_PAYMENT, PAID), validation (ids non vides). |
| `WaitlistSubscriptionTest` | Création, rehydrate, statuts (WAITING, NOTIFIED). |
| `ExternalEventTest` | Value object catalogue (id, nom, date, lieu). |
| `PriceRangeTest` | Intervalle de prix (min/max), validation. |
| `AvailabilityServiceTest` | Logique disponibilité / agrégats (selon implémentation). |
| `ExceptionContractsTest` | Contrat des exceptions métier (ErrorCode, messages). |

**Objectif :** Vérifier les invariants du domaine sans dépendance à Spring ou à la base.

---

### 3.2 Application (`marketplace-application`)

| Fichier de test | Scénarios couverts |
|-----------------|--------------------|
| `CreateListingUseCase` / `ListingCoreUseCasesTest` | Création d’annonce, certification, rejet, événements (ListingCertified, WaitlistTicketsAvailable). |
| `CertifyListingUseCaseTest` | Certification avec/sans waitlist, dispatch des bons événements. |
| `PlaceOrderUseCaseTest` | Création commande, calcul pricing, dispatch OrderPlacedApplicationEvent. |
| `GetOrderUseCaseTest` | Récupération commande par id, cas non trouvé. |
| `MarkOrderPaidUseCaseTest` | Passage en PAID, dispatch OrderPaidApplicationEvent, idempotence (déjà payée). |
| `SubscribeWaitlistUseCaseTest` | Inscription waitlist, doublon (refus), événement inexistant. |
| `UnsubscribeWaitlistUseCaseTest` | Désinscription, cas non trouvé. |
| `ProcessPaymentWebhookUseCaseTest` | Webhook PAID, mise à jour commande, idempotence. |
| `SendNotificationUseCaseTest` | Appel au template + envoi email (mock EmailSender). |
| `NotificationHandlersTest` | Chaque handler (ListingCertified, OrderPlaced, OrderPaid, WaitlistTicketsAvailable) : supports(event), handle() appelle SendNotificationUseCase avec les bons paramètres. |
| `NotificationTemplateFactoryTest` | Résolution du bon template par type d’événement. |
| `EmailTemplateStrategyTest` / `*TemplateStrategyTest` | Contenu sujet/texte/HTML selon le type (LISTING_CERTIFIED, ORDER_PLACED, ORDER_PAID, WAITLIST_TICKETS_AVAILABLE), payload manquant → NTF-001. |
| `SpringApplicationEventDispatcherTest` | Dispatch vers le handler qui supports(event), pas d’appel au handler qui ne supporte pas. |
| `UploadListingAttachmentUseCaseTest` / `GenerateListingAttachmentUploadUrlUseCaseTest` | Upload / presign avec ObjectStorage mock. |
| `CatalogUseCasesTest` | Recherche événements, get by id (catalogue mock). |

**Objectif :** Vérifier la logique applicative et les interactions (use case → port/event) avec mocks.

---

### 3.3 Infrastructure (`marketplace-infrastructure`)

| Fichier de test | Scénarios couverts |
|-----------------|--------------------|
| `EventApiIntegrationTest` | GET search, GET by id, événement inexistant (404). |
| `ListingApiIntegrationTest` | Création (auth vendeur), liste publique, certification (auth contrôleur), upload pièce jointe, presign, erreurs 401/403/400. |
| `OrderApiIntegrationTest` | Création commande (auth acheteur), GET order, marquer payée (auth contrôleur), erreurs (listing déjà vendu, etc.). |
| `WaitlistApiIntegrationTest` | Inscription, désinscription, doublon (409), événement inexistant, validation paramètres. |
| `PaymentWebhookIntegrationTest` | Webhook PAID, commande passée à PAID, email vendeur (FakeEmailSender), idempotence webhook. |
| `NotificationObserverIntegrationTest` | Parcours complet : création annonce → certification → email vendeur ; commande → email acheteur ; paiement → email vendeur ; waitlist → email « billets disponibles ». Vérification des sujets/corps (FakeEmailSender). |
| `ErrorHandlingIntegrationTest` | Contrat d’erreurs API (codes CAT/LST/ORD/PAY/WAI/AUTH), format de réponse. |
| `ApiExceptionHandlerTest` | Conversion des exceptions métier en réponses HTTP (status, error code). |
| `UserEntityTest` | Mapping JPA utilisateur. |
| `LocalObjectStorageAdapterTest` / `S3ObjectStorageAdapterTest` | Stockage local / contrat S3 (selon implémentation). |
| `StorageConfigTest` | Sélection du bon adapter (local vs S3) selon la config. |

**Objectif :** Vérifier les endpoints REST, l’authentification (JWT/Basic selon config), la chaîne complète jusqu’aux adapteurs (DB, email, stockage).

---

## 4. Synthèse

- **Scénarios** : couverture des cas nominaux, des erreurs métier (validation, doublon, non trouvé), des droits (rôles vendeur/acheteur/contrôleur) et de la non-régression des contrats (erreurs, templates email).
- **Couverture** : minimum **30 % de lignes** (JaCoCo), vérifié au `mvn verify` et en CI. Les rapports sont dans `*/target/site/jacoco/`.
- **Tests de mutation** : profil `mutation` (PIT) pour mesurer l’efficacité des TU ; optionnel en CI selon le workflow.

Ce document sert de référence pour la soutenance : quels scénarios sont couverts et où, et comment le taux de couverture est garanti (seuil 30 %, outil JaCoCo, intégration Maven/CI).
