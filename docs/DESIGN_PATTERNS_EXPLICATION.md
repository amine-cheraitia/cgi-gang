# Explication des Design Patterns — Ticketio

Contexte d'utilisation, exemples tirés du code et justification de chaque choix.

---

## 1. Static Factory Method

### Définition

Le **Static Factory Method** est une méthode statique qui remplace le constructeur public pour créer un objet. Au lieu de faire `new Order(...)`, on appelle `Order.place(...)`. C'est une variante du pattern **Factory Method** appliquée directement sur la classe elle-même.

---

### Usages dans le code

#### `Order.place()` — Création d'une commande

```java
// ✅ Avec Static Factory Method
Order order = Order.place(listingId, buyerId, sellerId, ticketPrice);

// ❌ Sans (constructeur brut — interdit dans notre domaine)
Order order = new Order(UUID.randomUUID().toString(), listingId, buyerId, sellerId, ...);
```

**Ce que fait `Order.place()` :**
1. Valide que `listingId`, `buyerId`, `sellerId` ne sont pas vides.
2. Appelle `PricingBreakdown.calculate(ticketPrice)` pour calculer tous les frais.
3. Fixe automatiquement le statut initial à `PENDING_PAYMENT`.
4. Génère l'UUID.
5. Retourne un `Order` valide et cohérent.

**Pourquoi ?** Le nom `place()` exprime l'**intention métier** : on *place* une commande. Un `new Order()` ne dit rien sur le contexte. De plus, le constructeur privé empêche de créer un `Order` dans un état invalide depuis l'extérieur.

---

#### `Order.rehydrate()` — Reconstruction depuis la BDD

```java
// Reconstituer un Order depuis les données JPA
Order order = Order.rehydrate(id, listingId, buyerId, sellerId, pricing, status);
```

**Ce que fait `Order.rehydrate()` :**
- Reconstruit l'objet **sans passer par les règles de création** (pas de validation, pas de calcul de prix).
- Le nom dit clairement : "je recharge un objet existant", pas "je crée quelque chose de nouveau".

**Pourquoi deux méthodes ?** `place()` = nouvelle commande avec règles métier. `rehydrate()` = objet existant récupéré de la BDD. Si on utilisait le même constructeur pour les deux, on ne pourrait pas distinguer les deux intentions ni appliquer les bonnes validations.

---

#### `Listing.create()` — Création d'une annonce

```java
Listing listing = Listing.create(externalEventId, sellerId, price);
```

**Ce que fait `Listing.create()` :**
1. Valide que `externalEventId` n'est pas null.
2. Valide que `sellerId` n'est pas blanc.
3. Valide que `price` n'est pas null et non négatif.
4. Fixe automatiquement le statut à `PENDING_CERTIFICATION`.

**Pourquoi ?** Une annonce créée doit **toujours** démarrer en `PENDING_CERTIFICATION`. Si on exposait le constructeur, rien n'empêcherait de créer une annonce directement en `CERTIFIED` — ce qui violerait une règle métier fondamentale.

---

#### `Money.euros()` / `Money.usd()` — Création de montants

```java
Money price      = Money.euros(89.00);
Money commission = Money.euros(new BigDecimal("8.90"));
Money dollarAmt  = Money.usd(100.0);
```

**Ce que fait `Money.euros()` :**
- Évite d'écrire `new Money(BigDecimal.valueOf(89.00), Currency.getInstance("EUR"))` partout.
- Le nom est expressif : "un montant en euros".
- Centralise la construction → si la logique change (ex. arrondi), un seul endroit à modifier.

---

#### `PricingBreakdown.calculate()` — Calcul du pricing

```java
PricingBreakdown pricing = PricingBreakdown.calculate(ticketPrice);
// Résultat :
// sellerFee      = prix × 5%
// serviceFee     = prix × 10%
// transactionFee = prix × 2.5%
// buyerTotal     = prix + serviceFee + transactionFee
// sellerPayout   = prix - sellerFee
// platformRevenue= sellerFee + serviceFee + transactionFee
// Invariant garanti : buyerTotal = sellerPayout + platformRevenue ✅
```

**Pourquoi ?** Le calcul des frais est une règle métier complexe. En le plaçant dans une Static Factory Method sur le Value Object lui-même, le calcul est **encapsulé, testé et centralisé**. Impossible d'obtenir un `PricingBreakdown` incohérent depuis l'extérieur.

---

#### `WaitlistSubscription.create()` — Inscription en liste d'attente

```java
WaitlistSubscription sub = WaitlistSubscription.create(eventId, userId);
// → génère automatiquement l'UUID et la date createdAt = maintenant
```

**Pourquoi ?** La date de création est une règle métier (elle est fixée **au moment de l'inscription**). En l'incluant dans la factory, elle ne peut pas être oubliée ou falsifiée depuis l'extérieur.

---

### Résumé — Ce qu'apporte Static Factory Method

| Problème sans le pattern | Solution apportée |
|---|---|
| Constructeur brut : aucun contrôle de l'état initial | Statut initial garanti dans la factory (`PENDING_CERTIFICATION`, `PENDING_PAYMENT`) |
| Code client complexe : doit calculer lui-même les frais | `PricingBreakdown.calculate()` encapsule toute la logique |
| Pas d'intention dans `new Order(...)` | `Order.place()` exprime l'action métier |
| Risque de créer des objets incohérents | Validation obligatoire avant construction |
| Duplication du code de construction | Un seul endroit à modifier |

---

---

## 2. IoC — Inversion of Control (Injection de Dépendances)

### Définition

L'**Inversion of Control** (IoC) est un principe selon lequel une classe ne crée pas elle-même ses dépendances : c'est un conteneur externe (ici Spring) qui les **injecte**. La variante la plus courante est la **Dependency Injection (DI)** par constructeur.

> "Ne m'appelle pas, je t'appellerai." — le conteneur IoC fournit ce dont tu as besoin.

---

### Usages dans le code

#### Use Case injecté par Spring

```java
@Component
public class PlaceOrderUseCase {

    private final ListingRepository    listingRepository;
    private final OrderRepository      orderRepository;
    private final PaymentGateway       paymentGateway;
    private final ApplicationEventDispatcher dispatcher;

    // Spring injecte automatiquement toutes les dépendances au démarrage
    public PlaceOrderUseCase(
            ListingRepository listingRepository,
            OrderRepository orderRepository,
            PaymentGateway paymentGateway,
            ApplicationEventDispatcher dispatcher) {
        this.listingRepository = listingRepository;
        this.orderRepository   = orderRepository;
        this.paymentGateway    = paymentGateway;
        this.dispatcher        = dispatcher;
    }
}
```

**Ce que fait Spring :** au démarrage, il détecte `@Component`, regarde le constructeur, trouve les beans correspondants (`JpaOrderRepositoryAdapter`, `StripePaymentGateway` ou `FakePaymentGateway`, etc.) et les injecte automatiquement.

**Pourquoi ?** `PlaceOrderUseCase` **ne sait pas** si le paiement est géré par Stripe ou un fake. Il travaille avec l'interface `PaymentGateway`. C'est le conteneur IoC qui décide quelle implémentation utiliser selon la configuration.

---

#### `@ConditionalOnProperty` — Sélection de l'adapter actif

```java
// Actif si catalog.provider=ticketmaster dans application.yml
@Component
@ConditionalOnProperty(name = "catalog.provider", havingValue = "ticketmaster")
public class TicketmasterCatalogProvider implements CatalogProvider {
    // Appelle l'API Ticketmaster
}

// Actif par défaut (matchIfMissing = true)
@Component
@ConditionalOnProperty(name = "catalog.provider", havingValue = "mock", matchIfMissing = true)
public class MockCatalogProvider implements CatalogProvider {
    // Retourne des données en dur
}
```

**Ce que fait Spring :** selon la valeur de `catalog.provider` dans le fichier de config (ou la variable d'environnement `CATALOG_PROVIDER`), Spring injecte soit `TicketmasterCatalogProvider`, soit `MockCatalogProvider` — **sans modifier une seule ligne de code**.

**Pourquoi ?** En dev/test : `CATALOG_PROVIDER=mock` → pas d'appel réseau, tests rapides. En prod : `CATALOG_PROVIDER=ticketmaster` → intégration réelle. **Zéro changement de code** entre les environnements.

Le même mécanisme est utilisé pour :
- `payment.provider=fake` → `FakePaymentGateway` (tests) / `payment.provider=stripe` → `StripePaymentGateway` (prod)
- `notification.provider=fake` → `FakeEmailSender` (tests) / `notification.provider=brevo` → `BrevoEmailSender` (prod)

---

#### `NotificationTemplateFactory` — IoC + Factory Method combinés

```java
@Component
public class NotificationTemplateFactory {

    private final List<EmailTemplateStrategy> strategies;

    // Spring injecte AUTOMATIQUEMENT toutes les implémentations de EmailTemplateStrategy
    public NotificationTemplateFactory(List<EmailTemplateStrategy> strategies) {
        this.strategies = strategies;
    }

    public EmailTemplateStrategy resolve(NotificationEventType eventType) {
        return strategies.stream()
            .filter(s -> s.supports(eventType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No template for " + eventType));
    }
}
```

**Ce que fait Spring :** il détecte que le constructeur demande une `List<EmailTemplateStrategy>` et injecte **toutes les classes annotées `@Component` qui implémentent cette interface** :
- `OrderPlacedTemplateStrategy`
- `OrderPaidTemplateStrategy`
- `ListingCertifiedTemplateStrategy`
- `WaitlistAvailableTemplateStrategy`

**Pourquoi ?** Pour ajouter un nouveau type d'email, il suffit de créer une nouvelle classe `@Component implements EmailTemplateStrategy`. Spring l'injecte automatiquement dans la factory. **Zéro modification de `NotificationTemplateFactory`** — c'est le principe Open/Closed.

---

#### `SpringApplicationEventDispatcher` — Observer via IoC

```java
@Component
public class SpringApplicationEventDispatcher implements ApplicationEventDispatcher {

    private final List<ApplicationEventHandler<?>> handlers;

    // Spring injecte tous les handlers détectés dans le contexte
    public SpringApplicationEventDispatcher(List<ApplicationEventHandler<?>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void dispatch(ApplicationEvent event) {
        handlers.stream()
            .filter(h -> h.supports(event))
            .forEach(h -> handleUnchecked(h, event));
    }
}
```

**Ce que fait Spring :** il injecte automatiquement tous les beans `ApplicationEventHandler` :
- `OrderPlacedNotificationHandler`
- `OrderPaidNotificationHandler`
- `ListingCertifiedNotificationHandler`
- `WaitlistAvailableNotificationHandler`

**Pourquoi ?** Ajouter un nouveau handler (ex : envoyer un SMS) = créer une classe `@Component implements ApplicationEventHandler`. Le dispatcher ne change pas. C'est IoC + Observer combinés.

---

#### Injection dans les Controllers REST

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PlaceOrderUseCase  placeOrderUseCase;
    private final GetOrderUseCase    getOrderUseCase;
    private final MarkOrderPaidUseCase markOrderPaidUseCase;

    // Spring injecte les use cases
    public OrderController(PlaceOrderUseCase placeOrderUseCase,
                           GetOrderUseCase getOrderUseCase,
                           MarkOrderPaidUseCase markOrderPaidUseCase) {
        this.placeOrderUseCase   = placeOrderUseCase;
        this.getOrderUseCase     = getOrderUseCase;
        this.markOrderPaidUseCase = markOrderPaidUseCase;
    }
}
```

**Pourquoi ?** Le Controller ne sait pas comment les use cases sont construits ni quelles dépendances ils ont. Il reçoit des objets prêts à l'emploi. Si on change l'implémentation d'un use case, le Controller n'est pas impacté.

---

### Résumé — Ce qu'apporte IoC / Injection de Dépendances

| Problème sans le pattern | Solution apportée |
|---|---|
| Classe qui crée ses dépendances = couplage fort | Spring injecte via le constructeur = couplage faible |
| Changer de prestataire (Stripe → autre) = modifier le code | `@ConditionalOnProperty` switch l'adapter via config |
| Tester un Use Case = démarrer Spring, BDD, Stripe... | Mockito injecte des fakes dans le constructeur |
| Ajouter un handler/template = modifier la factory | Spring découvre automatiquement les nouveaux `@Component` |
| Ordre de création des objets à gérer manuellement | Spring gère le cycle de vie et les dépendances circulaires |

---

## 3. Lien entre les deux patterns

Dans Ticketio, les deux patterns se complètent :

```
IoC (Spring)
    → injecte CatalogProvider (interface)
        → qui est soit MockCatalogProvider soit TicketmasterCatalogProvider

Static Factory Method
    → garantit que Listing.create(), Order.place(), Money.euros()
      produisent des objets valides avec les règles métier respectées
```

- **Static Factory Method** protège la **cohérence du domaine** : aucun objet invalide ne peut être créé.
- **IoC** protège la **flexibilité de l'infrastructure** : aucune classe ne dépend directement d'une implémentation concrète.

Ensemble, ils permettent de respecter deux principes SOLID fondamentaux :
- **Open/Closed Principle** (OCP) : ouvert à l'extension, fermé à la modification.
- **Dependency Inversion Principle** (DIP) : dépendre des abstractions (interfaces), pas des implémentations.
