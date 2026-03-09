# Guide de démo API – Marketplace revente de billets

Ce guide décrit un **parcours complet** pour démontrer l’API. Tu peux utiliser **Postman**, **curl** ou **Swagger UI** (`http://localhost:8080/swagger-ui.html`).

**Prérequis :** l’application tourne sur `http://localhost:8080` (ou le port configuré dans `application.yml`).

---

## Logique métier du parcours

En suivant la logique du métier :

1. **La vente commence** — Le vendeur crée une annonce (mise en vente d’un billet pour un événement).
2. **Le contrôleur authentifie le billet** — Une annonce est d’abord en attente ; le contrôleur la certifie pour qu’elle devienne achetable.
3. **Pour finir, l’achat** — L’acheteur passe commande sur une annonce certifiée, puis le paiement est confirmé.

Résumé : **Vente (annonce) → Certification du billet → Achat (commande + paiement)**.

---

## 1. Démarrer l’application

```bash
# À la racine du projet (cgi-gang)
./mvnw spring-boot:run -pl marketplace-infrastructure
```

Ou depuis ton IDE : lancer la classe `MarketplaceApplication`.

---

## 2. Ordre des appels (aligné sur la logique métier)

| Étape | Phase | Rôle      | Endpoint | Action |
|-------|--------|-----------|----------|--------|
| 1–2   | (contexte) | Public | Catalogue | Voir / choisir un événement |
| **3** | **1. Vente** | Vendeur | Listings | Créer une annonce (mise en vente) |
| **4** | **2. Certification** | Contrôleur | Certification | Authentifier / certifier le billet |
| 5     | (vérif) | Public | Listings | Voir les annonces disponibles (certifiées) |
| **6–8** | **3. Achat** | Acheteur / Contrôleur | Orders | Passer commande → consulter → marquer payée |

Optionnel : **waitlist** (s’inscrire en attente) et **webhook paiement**.

---

## 3. Détail des appels

### Comptes de démo (Basic Auth)

| Rôle        | Username   | Password      | Usage |
|------------|------------|---------------|--------|
| Vendeur    | `seller`   | `seller123`   | Créer annonces |
| Acheteur   | `buyer`   | `buyer123`    | Commander, waitlist |
| Contrôleur | `controller` | `controller123` | Certifier annonces, marquer payé |

IDs métier à utiliser dans les body : `sellerId`: **`seller`**, `buyerId`: **`buyer`**.  
Événements du catalogue mock : `evt_taylor_paris`, `evt_psg_om`, `evt_coldplay_nanterre`, `evt_burna_dakar`.

---

### Étape 1 – Rechercher des événements (public, pas d’auth)

```http
GET http://localhost:8080/api/events/search?query=Taylor
```

Réponse : liste d’événements (ex. Taylor Swift - The Eras Tour).

---

### Étape 2 – Détail d’un événement (public)

```http
GET http://localhost:8080/api/events/evt_taylor_paris
```

Tu récupères l’id, le titre, la date, le lieu. **Conserve `evt_taylor_paris`** pour les étapes suivantes.

---

### Étape 3 – Créer une annonce (vendeur)

**Auth :** Basic `seller` / `seller123`

```http
POST http://localhost:8080/api/listings
Content-Type: application/json

{
  "eventId": "evt_taylor_paris",
  "sellerId": "seller",
  "price": 89.00,
  "currency": "EUR"
}
```

Réponse **201** avec un `id` (ex. `lst_xxx`). **Note cet `id`** → c’est ton `listingId` pour la certification et la commande.

---

### Étape 4 – Le contrôleur authentifie le billet (certification)

**Auth :** Basic `controller` / `controller123`

```http
POST http://localhost:8080/api/certification/{listingId}/certify
```

Remplace `{listingId}` par l’id reçu à l’étape 3 (ex. `POST .../api/certification/lst_abc123/certify`).

Réponse **200** avec `"status": "CERTIFIED"`. Le billet est authentifié ; l’annonce devient visible côté “billets disponibles”.

---

### Étape 5 – Lister les annonces disponibles (public)

```http
GET http://localhost:8080/api/listings
```

Tu dois voir ton annonce avec le statut `CERTIFIED`. Tu peux utiliser son `id` pour la commande si tu ne l’as plus sous la main.

---

### Étape 6 – Passer une commande (acheteur)

**Auth :** Basic `buyer` / `buyer123`

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "listingId": "<listingId de l'étape 3>",
  "buyerId": "buyer"
}
```

Réponse **201** avec `orderId`. **Note cet `orderId`** pour les étapes 7 et 8.

---

### Étape 7 – Consulter la commande (acheteur)

**Auth :** Basic `buyer` / `buyer123`

```http
GET http://localhost:8080/api/orders/{orderId}
```

Tu vois le détail de la commande et le pricing.

---

### Étape 8 – Marquer la commande comme payée (contrôleur)

**Auth :** Basic `controller` / `controller123`

```http
POST http://localhost:8080/api/orders/{orderId}/pay
```

Réponse **200** avec le statut de la commande mis à jour (paiement confirmé).

---

## 4. Bonus : waitlist (optionnel)

Un acheteur peut s’inscrire en liste d’attente pour un événement (ex. pour être notifié si des billets reviennent).

**S’inscrire :**  
**Auth :** `buyer` / `buyer123`

```http
POST http://localhost:8080/api/waitlist/subscriptions
Content-Type: application/json

{
  "eventId": "evt_taylor_paris",
  "userId": "buyer"
}
```

**Se désinscrire :**

```http
DELETE http://localhost:8080/api/waitlist/subscriptions?eventId=evt_taylor_paris&userId=buyer
```

---

## 5. Bonus : annonces en attente de certification (contrôleur)

**Auth :** `controller` / `controller123`

```http
GET http://localhost:8080/api/certification/pending
```

Liste toutes les annonces en `PENDING_CERTIFICATION` (à certifier ou refuser).

---

## 6. Résumé “ordre des endpoints” pour la démo

**Contexte (catalogue)**  
1. **GET** `/api/events/search?query=Taylor` → choisir un événement  
2. **GET** `/api/events/evt_taylor_paris` → détail événement  

**1. Vente**  
3. **POST** `/api/listings` (seller) → créer l’annonce (la vente commence) → récupérer `listingId`  

**2. Le contrôleur authentifie le billet**  
4. **POST** `/api/certification/{listingId}/certify` (controller) → certifier l’annonce  

**Vérification**  
5. **GET** `/api/listings` → vérifier que l’annonce certifiée est visible  

**3. Achat (pour finir)**  
6. **POST** `/api/orders` (buyer) → créer la commande → récupérer `orderId`  
7. **GET** `/api/orders/{orderId}` (buyer) → détail commande  
8. **POST** `/api/orders/{orderId}/pay` (controller) → confirmer le paiement  

Ensuite : waitlist et webhook paiement en option pour la notification et le paiement externe.

---

## 7. Postman

La collection **`postman/Marketplace-Revente-Billets.postman_collection.json`** contient le parcours “RUN - Parcours Evaluatrice” dans le même ordre.  
Variables à définir (ou par défaut) :  
`baseUrl` = `http://localhost:8080`, `eventId` = `evt_taylor_paris`, `sellerId` = `seller`, `buyerId` = `buyer`, et les identifiants des comptes ci-dessus.  
Après chaque création (listing, order), les scripts peuvent enregistrer `listingId` et `orderId` pour les requêtes suivantes.

---

## 8. Swagger

Une fois l’app démarrée : **http://localhost:8080/swagger-ui.html**  
Tu peux exécuter tous les endpoints depuis l’interface. Pour les appels protégés, utilise “Authorize” avec Basic Auth (seller/buyer/controller selon l’étape).
