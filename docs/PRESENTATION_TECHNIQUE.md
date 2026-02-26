# Présentation technique du projet Ticketio

Document de soutenance : outils, langages, dépendances externes et découpage technique avec justification des choix.

---

## 1. Langage et plateforme

### Java 21 (LTS)
**Choix :** Java 21, version LTS (Long-Term Support).

**Justification :**
- Support long terme garanti (Oracle + communauté) → stabilité en production.
- **Records** Java 16+ : utilisés pour les Value Objects (`Money`, `PricingBreakdown`, `ExternalEvent`, `EmailMessage`) — immutabilité native, equals/hashCode/toString générés automatiquement.
- **Text blocks** Java 15+ : utilisés dans les templates HTML email (`EmailHtmlLayout`) pour des templates multi-lignes lisibles.
- **Pattern matching** et **sealed classes** disponibles → expressivité DDD.
- Compatibilité totale avec Spring Boot 3.x (qui exige Java 17 minimum).

---

## 2. Framework principal

### Spring Boot 3.4.1
**Choix :** Spring Boot, version 3.4.1 (dernière stable au moment du développement).

**Justification :**
- **Convention over configuration** : démarrage rapide, auto-configuration des composants (JPA, Security, Web).
- **IoC / Injection de dépendances** : essentiel à l'architecture hexagonale — les ports sont des interfaces, les adapters sont injectés automatiquement par Spring (`@Component`, `@ConditionalOnProperty`).
- `@ConditionalOnProperty` : permet de sélectionner l'adapter actif à la configuration (mock/réel) sans modifier le code du domaine.
- Écosystème riche et mature : Spring Security, Spring Data JPA, Spring Validation, Spring Web MVC.
- Support natif de Docker (build image, health checks) et des profils Spring (`test`, `prod`).

---

## 3. Outil de build

### Maven 3.9 (multi-module)
**Choix :** Maven avec structure multi-module (`marketplace-domain`, `marketplace-application`, `marketplace-infrastructure`).

**Justification :**
- **Séparation stricte des dépendances** : le module `domain` n'a aucune dépendance Spring ou JPA — c'est du Java pur. Cela est enforced par Maven (pas de `<dependency>` vers des frameworks externes dans `marketplace-domain/pom.xml`).
- **Ordre de build garanti** : Maven compile domain → application → infrastructure dans le bon ordre, assurant que les couches ne peuvent pas se référencer à l'envers.
- Universel, intégré nativement avec les CI/CD (GitHub Actions, Jenkins, etc.).
- Maven Wrapper (`mvnw`) inclus : aucune installation locale requise pour builder le projet.

---

## 4. Architecture logicielle

### Clean Architecture + Architecture Hexagonale (Ports & Adapters)

**Structure des modules :**

| Module Maven | Couche | Contenu |
|---|---|---|
| `marketplace-domain` | Domaine | Entités, Agrégats, Value Objects, interfaces Repository (Ports), Services domaine |
| `marketplace-application` | Application | Use Cases, Event Handlers, Ports applicatifs (EmailSender), Templates email |
| `marketplace-infrastructure` | Infrastructure | Controllers REST, Adapters JPA, Adapters email/paiement/stockage/catalogue, Config Spring |

**Justification :**
- Le domaine métier est **complètement indépendant** des frameworks (Spring, JPA, Stripe, etc.) → testable sans contexte Spring.
- Les **Ports** (interfaces) dans le domaine/application définissent ce dont le système a besoin ; les **Adapters** dans l'infrastructure fournissent les implémentations réelles.
- Changement de prestataire (ex. : Brevo → SendGrid, Stripe → PayPal) = implémenter un nouveau Adapter, **zéro modification du domaine**.
- Chaque couche est testée indépendamment : TU domaine (pur Java), TU application (Mockito), TI infrastructure (Spring Boot Test).

### Domain-Driven Design (DDD)

**Bounded Contexts :** `CATALOG`, `LISTING`, `SALES`, `PAYMENT`, `WAITLIST`, `NOTIFICATION`

**Éléments DDD implémentés :**

| Concept DDD | Implémentation |
|---|---|
| **Aggregate** | `Listing`, `Order`, `WaitlistSubscription` |
| **Value Object** | `Money`, `PricingBreakdown`, `ExternalEventId`, `PriceRange` |
| **Domain Service** | `AvailabilityService`, calcul `PricingBreakdown` |
| **Repository (Port)** | `ListingRepository`, `OrderRepository`, `WaitlistSubscriptionRepository` |
| **Domain Event** | `OrderPlacedApplicationEvent`, `OrderPaidApplicationEvent`, `ListingCertifiedApplicationEvent` |
| **Application Event Dispatcher** | `SpringApplicationEventDispatcher` (pattern Observer) |

---

## 5. Dépendances techniques

### 5.1 Persistance

| Dépendance | Version | Rôle | Justification |
|---|---|---|---|
| **Spring Data JPA + Hibernate** | (Spring Boot BOM) | ORM — mapping objets ↔ tables SQL | Abstraction de la persistance derrière des `Repository` interfaces ; le domaine ne connaît pas JPA |
| **H2** | runtime | Base de données en mémoire (dev/test) | Pas de PostgreSQL à installer en local ou en CI ; tests ultra-rapides |
| **Liquibase** | (Spring Boot BOM) | Gestion du schéma SQL (migrations versionnées) | Schéma reproductible à chaque démarrage ; migrations `001` à `006` (users, listings, orders, waitlist, stripe, passwords) |
| **PostgreSQL** | prod | Base de données relationnelle en production | Robustesse, conformité SQL, scalabilité ; switch H2→PostgreSQL via configuration uniquement |

### 5.2 API REST & Documentation

| Dépendance | Version | Rôle | Justification |
|---|---|---|---|
| **Spring Web MVC** | (Spring Boot BOM) | Framework REST | Natif Spring Boot, contrôleurs `@RestController`, validation `@Valid` |
| **Spring Validation** | (Spring Boot BOM) | Validation des payloads entrants | Annotations `@NotBlank`, `@Pattern` sur les DTOs ; erreurs standardisées |
| **Springdoc OpenAPI** | 2.7.0 | Documentation Swagger auto-générée | Interface Swagger UI accessible sur `/swagger-ui` ; contrat API lisible sans documentation manuelle |

### 5.3 Sécurité

| Dépendance | Version | Rôle | Justification |
|---|---|---|---|
| **Spring Security** | (Spring Boot BOM) | Authentification + Autorisation | HTTP Basic Auth ; contrôle d'accès par rôle (`SELLER`, `BUYER`, `CONTROLLER`) sur chaque endpoint ; gestion des erreurs 401/403 |

### 5.4 Intégrations externes

| Dépendance | Version | Rôle | Justification |
|---|---|---|---|
| **stripe-java** | 27.1.0 | SDK officiel Stripe | Création de `PaymentIntent`, vérification des signatures webhook (`Webhook.constructEvent`) ; SDK mature et maintenu |
| **AWS SDK S3** | 2.41.27 | Stockage de fichiers S3 | Upload/download des pièces justificatives (`PutObjectRequest`, presigned URLs) ; abstraction derrière le port `ObjectStorage` |
| **Brevo (Sendinblue) API** | REST HTTP | Envoi d'emails transactionnels | Appels directs REST (`POST /v3/smtp/email`) avec `RestTemplate` ; pas de SDK tiers — dépendance légère |
| **Ticketmaster Discovery API v2** | REST HTTP | Catalogue d'événements externe | Recherche par keyword, récupération par ID ; abstraction derrière le port `CatalogProvider` |

### 5.5 Tests

| Dépendance | Version | Rôle | Justification |
|---|---|---|---|
| **JUnit 5** | (Spring Boot BOM) | Framework de tests unitaires et d'intégration | Standard Java ; annotations `@Test`, `@DisplayName`, `@Nested` |
| **Mockito** | (Spring Boot BOM) | Mocking des dépendances | Isolation des Use Cases lors des TU ; `verify()` pour asserter les comportements |
| **AssertJ** | (Spring Boot BOM) | Assertions fluentes | Lisibilité des tests (`assertThat(...).contains(...)`) |
| **Spring Boot Test** | (Spring Boot BOM) | Tests d'intégration Spring | `@SpringBootTest`, `MockMvc`, `@TestPropertySource` pour les TI REST avec contexte complet |
| **Spring Security Test** | (Spring Boot BOM) | Tests des endpoints sécurisés | `@WithMockUser`, encodage Basic Auth dans les TI |
| **JaCoCo** | 0.8.12 | Couverture de code | Gate de couverture ligne ≥ 30% enforced au `mvn verify` et en CI ; rapports HTML générés |
| **PIT (Pitest)** | 1.22.1 | Tests de mutation | Vérifie la qualité des tests en injectant des mutations dans le bytecode ; seuil 70% |

### 5.6 Infrastructure & DevOps

| Outil | Rôle | Justification |
|---|---|---|
| **Docker + Dockerfile multi-stage** | Conteneurisation | Build (`maven:3.9-eclipse-temurin-21`) + Runtime (`eclipse-temurin:21-jre-alpine`) séparés → image légère (~150 MB) |
| **Docker Compose** | Orchestration locale | Démarrage en une commande (`docker compose up`) ; volumes persistants pour le stockage local |
| **GitHub Actions** | CI/CD | Pipeline automatique : `mvn clean verify` + build Docker à chaque push ; détection précoce des régressions |
| **Git pre-push hook** | Qualité locale | Lance `mvn clean verify` avant chaque push pour éviter de casser la CI |
| **.env + DotEnvEnvironmentPostProcessor** | Configuration | Variables sensibles (clés API) hors du code source ; chargées automatiquement par Spring au démarrage |

---

## 6. Découpage technique du projet

```
ticketio/
├── marketplace-domain/          # 🟡 Java pur — zéro dépendance framework
│   ├── catalog/domain/          # ExternalEvent, CatalogProvider (port)
│   ├── listing/domain/          # Listing (aggregate), ListingStatus, PriceRange
│   ├── sales/domain/            # Order (aggregate), PricingBreakdown, OrderStatus
│   ├── waitlist/domain/         # WaitlistSubscription
│   └── shared/domain/           # Money (VO), ErrorCode, BusinessException
│
├── marketplace-application/     # 🔵 Logique applicative — dépend uniquement du domain
│   ├── catalog/application/     # SearchEventsUseCase, GetEventByIdUseCase
│   ├── listing/application/     # CreateListingUC, CertifyListingUC, Upload...
│   ├── sales/application/       # PlaceOrderUseCase, GetOrderUseCase, MarkOrderPaidUC
│   ├── payment/application/     # ProcessPaymentWebhookUseCase
│   ├── waitlist/application/    # SubscribeWaitlistUC, UnsubscribeWaitlistUC
│   ├── notification/application/# SendNotificationUC, Handlers, Templates, Factory
│   └── shared/application/      # ApplicationEventDispatcher, ApplicationEvent
│
└── marketplace-infrastructure/  # 🔴 Framework + I/O — dépend de application + domain
    ├── catalog/infrastructure/  # EventController, MockCatalogProvider, TicketmasterCatalogProvider
    ├── listing/infrastructure/  # ListingController, CertificationController, JpaListingRepositoryAdapter
    ├── sales/infrastructure/    # OrderController, JpaOrderRepositoryAdapter
    ├── payment/infrastructure/  # PaymentWebhookController, StripeWebhookController, StripePaymentGateway
    ├── waitlist/infrastructure/ # WaitlistController, JpaWaitlistSubscriptionRepositoryAdapter
    ├── notification/infra/      # BrevoEmailSender, FakeEmailSender
    ├── storage/infrastructure/  # LocalObjectStorageAdapter, S3ObjectStorageAdapter
    ├── user/infrastructure/     # AuthController, UserEntity, DatabaseUserDetailsService
    ├── security/                # SecurityConfig
    └── config/                  # DotEnvEnvironmentPostProcessor, OpenApiConfig
```

**Règle de dépendance (enforced par Maven) :**
```
infrastructure  →  application  →  domain
         ↑               ↑            ↑
    frameworks        use cases    métier pur
```
Jamais l'inverse : le domaine ne connaît ni Spring, ni JPA, ni Stripe.

---

## 7. Synthèse des choix techniques

| Décision | Alternative écartée | Raison du choix |
|---|---|---|
| Java 21 | Java 17 | Records natifs, text blocks, LTS récent |
| Spring Boot 3.x | Quarkus / Micronaut | Maturité, écosystème, facilité d'intégration JPA/Security |
| Architecture hexagonale | MVC classique en couches | Testabilité, remplacement de prestataires sans impact domaine |
| Maven multi-module | Gradle / mono-module | Isolation enforced des dépendances par module |
| H2 en dev + Liquibase | Testcontainers PostgreSQL | Démarrage instantané en CI sans Docker-in-Docker |
| Stripe | PayPal / Braintree | SDK Java officiel mature, webhooks signés, simulation locale |
| Brevo | SendGrid / Mailjet | API simple, compte gratuit généreux, templates HTML |
| JaCoCo + PIT | SonarQube uniquement | Léger, intégré Maven, gate de couverture et mutation en CI |
