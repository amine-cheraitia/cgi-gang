# Guide de démo API – Marketplace revente de billets

Ce guide décrit un **parcours complet** pour démontrer l’API. Tu peux utiliser **Postman**, **curl** ou **Swagger UI** (`http://localhost:8080/swagger-ui.html`).

**Prérequis :** l’application tourne sur `http://localhost:8080`.

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

| Étape | Phase | Rôle technique | Endpoint | Action |
|-------|--------|----------------|----------|--------|
| 1–2   | (contexte) | Public | Catalogue | Voir / choisir un événement |
| 3     | (auth) | Client | Auth | Se connecter (JWT) |
| 4     | 1. Waitlist | Client | Waitlist | S’inscrire en liste d’attente sur l’événement |
| **5** | **2. Vente** | Client | Listings | Créer une annonce (mise en vente) |
| **6** | **3. Certification** | Contrôleur | Certification | Authentifier / certifier le billet |
| 7     | (vérif) | Public | Listings | Voir les annonces disponibles (certifiées) |
| **8–10** | **4. Achat** | Client / Contrôleur | Orders | Passer commande → consulter → marquer payée |

Optionnel : **webhook paiement** (intégration provider externe).

---

## 3. Détail des appels

### Comptes de démo & authentification (JWT)

L’API utilise des **tokens JWT Bearer**.  
Tu obtiens un token via `POST /api/auth/login` (clients) ou `POST /api/auth/admin/login` (back‑office).

**Comptes de démo :**

| Profil      | Username     | Password      | Rôles (BD)                 | Usage principal |
|------------|--------------|---------------|----------------------------|-----------------|
| Client 1   | `seller`     | `seller123`   | `CLIENT`                   | Acheter / vendre |
| Client 2   | `buyer`      | `buyer123`    | `CLIENT`                   | Acheter / vendre / waitlist |
| Contrôleur | `controller` | `controller123` | `CONTROLLER`             | Certifier, marquer payé |
| Admin      | `admin`      | `admin123`    | `ADMIN,CONTROLLER,CLIENT` | Tout (incl. back‑office) |

Événements du catalogue mock : `evt_taylor_paris`, `evt_psg_om`, `evt_coldplay_nanterre`, `evt_burna_dakar`.

#### Obtenir un token client (JWT)

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "buyer",
  "password": "buyer123"
}
```

Réponse :

```json
{
  "token": "<JWT>",
  "tokenType": "Bearer"
}
```

Tu ajoutes ensuite le header :

```http
Authorization: Bearer <JWT>
```

#### Obtenir un token admin / contrôleur

Pour l’espace d’admin (certification, marquage payé), tu peux utiliser :

```http
POST http://localhost:8080/api/auth/admin/login
Content-Type: application/json

{
  "username": "controller",
  "password": "controller123"
}
```

Seuls les comptes ayant un rôle `CONTROLLER` ou `ADMIN` sont acceptés sur cet endpoint.

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

### Étape 3 – Créer une annonce (client vendeur)

**Auth :** JWT d’un client (ex. token de `seller`)

```http
POST http://localhost:8080/api/listings
Content-Type: application/json

{
  "eventId": "evt_taylor_paris",
  "price": 89.00,
  "currency": "EUR"
}
```

Réponse **201** avec un `id` (ex. `lst_xxx`). **Note cet `id`** → c’est ton `listingId` pour la certification et la commande.

---

### Étape 4 – Le contrôleur authentifie le billet (certification)

**Auth :** JWT admin/contrôleur (token obtenu via `/api/auth/admin/login`)

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

### (Option) Uploader une pièce justificative pour l’annonce

Par défaut dans la démo, le stockage est configuré en **mode local** (`storage.provider=local`) :

- l’endpoint **upload direct** est **obligatoire** pour qu’une annonce puisse être certifiée.  
  Le fichier doit être un **PDF** (le backend refuse les autres types).

```http
POST http://localhost:8080/api/listings/{listingId}/attachments
Authorization: Bearer <JWT seller>
Content-Type: multipart/form-data

file = <ton_fichier_pdf>
```

- l’endpoint de **presign** (`POST /api/listings/{listingId}/attachments/presign`) renvoie volontairement une erreur métier `LST-005` (presign non disponible) tant que le provider n’est pas S3.

Quand tu passeras le stockage en S3, le même endpoint de presign commencera à retourner une URL `PUT` signée (upload directement vers S3), sans changement côté client.

---

### Étape 6 – Passer une commande (acheteur)

**Auth :** JWT d’un client (ex. token de `buyer`)

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "listingId": "<listingId de l'étape 3>"
}
```

Réponse **201** avec `orderId`. **Note cet `orderId`** pour les étapes 7 et 8.

---

### Étape 7 – Consulter la commande (acheteur)

**Auth :** JWT du client qui a passé la commande

```http
GET http://localhost:8080/api/orders/{orderId}
```

Tu vois le détail de la commande et le pricing.

---

### Étape 8 – Paiement confirmé (webhook) + mode manuel

Dans le flux normal, c’est le **provider de paiement** (Stripe ou `fake`) qui confirme le paiement via un **webhook**.

#### 8.a – Webhook `PAID` (flux principal)

Le provider appelle :

```http
POST http://localhost:8080/api/payments/webhooks
X-Payment-Webhook-Token: <ton_token_webhook>
Content-Type: application/json

{
  "orderId": "<orderId de l'étape 6>",
  "status": "PAID",
  "providerTransactionId": "tx_123"
}
```

Si le token et la charge utile sont valides, la commande passe en `PAID` et les emails correspondants sont envoyés.

#### 8.b – Mode manuel (fallback contrôleur)

En cas de problème technique (webhook non reçu, régularisation manuelle), un **contrôleur** peut forcer la mise à jour :

**Auth :** JWT admin/contrôleur

```http
POST http://localhost:8080/api/orders/{orderId}/pay
```

À utiliser uniquement comme **plan B** quand le webhook n’a pas pu jouer son rôle.

---

## 4. Waitlist (obligatoire dans la démo)

La démo inclut aussi l’expérience **liste d’attente** : un client peut s’inscrire sur un événement, puis être notifié quand un listing certifié apparaît.

**S’inscrire :**  
**Auth :** JWT d’un client (ex. token de `buyer`)

```http
POST http://localhost:8080/api/waitlist/subscriptions
Content-Type: application/json

{
  "eventId": "evt_taylor_paris"
}
```

**Se désinscrire :**

```http
DELETE http://localhost:8080/api/waitlist/subscriptions?eventId=evt_taylor_paris
```

---

## 5. Bonus : annonces en attente de certification (contrôleur)

**Auth :** JWT admin/contrôleur

```http
GET http://localhost:8080/api/certification/pending
```

Liste toutes les annonces en `PENDING_CERTIFICATION` (à certifier ou refuser).

---

## 6. Résumé “ordre des endpoints” pour la démo

**Contexte (catalogue)**  
1. **GET** `/api/events/search?query=Taylor` → choisir un événement  
2. **GET** `/api/events/evt_taylor_paris` → détail événement  

**1. Authentification client**  
3. **POST** `/api/auth/login` → obtenir un JWT client (buyer/seller)  

**2. Waitlist**  
4. **POST** `/api/waitlist/subscriptions` (JWT client) → inscrire le client en liste d’attente sur l’événement  

**3. Vente**  
5. **POST** `/api/listings` (JWT client) → créer l’annonce (la vente commence) → récupérer `listingId`  

**4. Le contrôleur authentifie le billet**  
6. **POST** `/api/certification/{listingId}/certify` (JWT admin/contrôleur) → certifier l’annonce  

**Vérification**  
7. **GET** `/api/listings` → vérifier que l’annonce certifiée est visible  

**5. Achat (pour finir)**  
8. **POST** `/api/payments/webhooks` (provider) → envoie un webhook `PAID` pour `orderId` → la commande passe en `PAID`  
9. **(Fallback)** `POST /api/orders/{orderId}/pay` (JWT admin/contrôleur) → à utiliser uniquement si le webhook ne peut pas être appelé  

Le webhook est donc le **moyen standard** de confirmer le paiement ; le contrôleur dispose d’un endpoint manuel de secours.

---

## 7. Postman

La collection **`postman/Marketplace-Revente-Billets.postman_collection.json`** contient le parcours “RUN - Parcours Evaluatrice” dans le même ordre.  

Variables principales (par défaut) :  
- `baseUrl` = `http://localhost:8080`  
- `eventId` = `evt_taylor_paris`  
- `sellerUsername` / `sellerPassword` / `buyerUsername` / `buyerPassword` / `controllerUsername` / `controllerPassword`  
- `jwtSeller`, `jwtBuyer` (remplis automatiquement par les requêtes de login de la section `00 - Auth`)

Le runner exécute : login → enregistre les JWT → utilise les headers `Authorization: Bearer {{jwtSeller}}` / `{{jwtBuyer}}` sur les endpoints protégés, puis enchaîne les étapes (listing, certification, commande, paiement).

---

## 8. Swagger

Une fois l’app démarrée : **http://localhost:8080/swagger-ui.html**

Dans Swagger UI :
- commence par appeler `POST /api/auth/login` (ou `/api/auth/admin/login`) pour obtenir un token,  
- clique sur le bouton **Authorize**, choisis le schéma `bearer-jwt` et colle `Bearer <JWT>` comme valeur.  
Tous les endpoints protégés utiliseront alors automatiquement ce token.

