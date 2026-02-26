# Présentation fonctionnelle du projet Ticketio

Document de présentation pour la soutenance : besoins utilisateurs et découpage fonctionnel.

---

## 1. Contexte et objectif du produit

**Ticketio** est une plateforme de **revente de billets certifiés** : elle permet à des vendeurs de proposer des billets d’événements (concerts, spectacles, sport, etc.) et à des acheteurs de les acquérir dans un cadre sécurisé.

**Objectifs métier :**
- Centraliser l’offre de billets autour d’un **catalogue d’événements** (intégration type Ticketmaster).
- Garantir la **qualité des annonces** via une **certification** par un rôle dédié (contrôleur).
- Sécuriser les **transactions** (commandes, paiements, commissions plateforme).
- Informer les utilisateurs par **notifications email** (commande, paiement, certification, liste d’attente).
- Proposer une **liste d’attente** par événement pour notifier les acheteurs dès que des billets redeviennent disponibles.

---

## 2. Besoins utilisateurs

Les besoins sont exprimés par **type d’acteur** : chaque rôle a des objectifs et des droits distincts.

### 2.1 Vendeur (SELLER)

| Besoin | Description | Réponse fonctionnelle |
|--------|-------------|------------------------|
| Proposer des billets à la vente | Pouvoir créer une annonce liée à un événement du catalogue, avec un prix. | Création d’annonce (listing) en statut « en attente de certification ». |
| Justifier l’authenticité | Fournir une pièce justificative (preuve d’achat, billet, etc.). | Upload de pièce jointe sur l’annonce (fichier ou URL présignée S3). |
| Savoir quand son annonce est validée | Être informé une fois la certification effectuée. | Email « Votre billet est certifié » avec lien vers l’annonce. |
| Savoir quand sa vente est payée | Être informé du paiement pour livraison / suivi. | Email « Paiement confirmé » avec détail (montant vendeur, commission plateforme). |
| Gérer ses annonces | Consulter / gérer les annonces qu’il a créées. | Accès aux endpoints listings (création, upload pièce jointe, présignée). |

**Résumé :** Le vendeur a besoin de **publier des annonces**, de les **justifier**, d’être **notifié** à la certification et au paiement, et de **gérer ses annonces**.

---

### 2.2 Acheteur (BUYER)

| Besoin | Description | Réponse fonctionnelle |
|--------|-------------|------------------------|
| Découvrir les événements | Trouver concerts, spectacles, matchs, etc. | Consultation du catalogue (recherche, détail par événement). |
| Voir les billets disponibles | Savoir quels billets certifiés sont en vente. | Liste des annonces certifiées (consultation publique, sans auth). |
| Acheter un billet | Passer commande sur une annonce certifiée. | Création de commande (order) ; calcul du prix acheteur (commission incluse). |
| Être rassuré sur la commande | Confirmation immédiate de la prise en compte. | Email « Commande enregistrée » avec récap (orderId, montant total). |
| Attendre des billets pour un événement complet | S’inscrire pour être prévenu si des places se libèrent. | Inscription à la liste d’attente par événement. |
| Être prévenu quand des billets arrivent | Ne pas manquer une nouvelle disponibilité. | Email « Des billets sont disponibles » avec lien et prix à partir de. |
| Se désinscrire de la waitlist | Ne plus recevoir d’alertes pour un événement. | Désinscription (eventId + userId). |

**Résumé :** L’acheteur a besoin de **consulter le catalogue et les annonces**, de **commander**, d’être **notifié** (commande + liste d’attente), et de **gérer son inscription en waitlist**.

---

### 2.3 Contrôleur (CONTROLLER)

| Besoin | Description | Réponse fonctionnelle |
|--------|-------------|------------------------|
| Voir les annonces à valider | Traiter les annonces en attente de certification. | Liste des annonces au statut « PENDING_CERTIFICATION ». |
| Certifier ou rejeter | Valider l’authenticité / conformité avant mise en vente. | Action « Certifier » : passage en « CERTIFIED » (visible sur le marché) ou rejet. |
| Confirmer les paiements reçus | Marquer une commande comme payée après réception du paiement. | Endpoint « Marquer commande payée » (ordre → PAID, déclenchement email vendeur). |

**Résumé :** Le contrôleur a besoin de **lister les annonces en attente**, de **certifier** (ou rejeter), et de **confirmer les paiements** pour clôturer le cycle de vente.

---

### 2.4 Utilisateur non authentifié

| Besoin | Description | Réponse fonctionnelle |
|--------|-------------|------------------------|
| Parcourir le catalogue | Découvrir les événements sans compte. | Recherche et détail d’événements (GET publics). |
| Voir les billets en vente | Comparer les offres avant de s’inscrire. | Liste des annonces certifiées (GET public). |
| S’inscrire | Créer un compte pour acheter ou vendre. | Inscription (register) avec rôle SELLER ou BUYER. |

**Résumé :** Consultation **publique** du catalogue et des annonces ; **inscription** pour accéder aux actions métier.

---

## 3. Découpage fonctionnel du projet

Le projet est découpé en **blocs fonctionnels** alignés sur les bounded contexts métier et exposés via l’API REST.

---

### 3.1 Authentification et utilisateurs (Auth)

- **Inscription** : création de compte (email, username, password, rôle SELLER ou BUYER).
- **Profil connecté** : endpoint « moi » (GET /api/auth/me) pour vérifier l’authentification et récupérer l’identité (HTTP Basic).
- **Sécurité** : authentification HTTP Basic ; rôles SELLER, BUYER, CONTROLLER ; contrôle d’accès par endpoint.

**Périmètre :** Gestion de l’identité et des droits d’accès aux autres blocs.

---

### 3.2 Catalogue d’événements (Catalog)

- **Recherche d’événements** : recherche par mot-clé (nom, lieu, ville, etc.) dans le catalogue externe (ex. Ticketmaster Discovery API v2).
- **Détail d’un événement** : récupération d’un événement par identifiant (nom, date, lieu, ville).

**Périmètre :** Alimentation du référentiel d’événements ; pas de création d’événements côté Ticketio (lecture seule sur la source externe).  
**Accès :** Public (lecture).

---

### 3.3 Annonces de billets (Listings)

- **Création d’annonce** : association événement (eventId) + vendeur (sellerId) + prix (et devise) ; statut initial « PENDING_CERTIFICATION ».
- **Liste des annonces certifiées** : annonces au statut « CERTIFIED » et disponibles à l’achat (consultation publique).
- **Pièce justificative** :  
  - upload direct d’un fichier (multipart) ;  
  - ou génération d’une URL présignée (S3) pour upload côté client.  
  Stockage abstrait (local ou S3 selon configuration).

**Périmètre :** Cycle de vie des annonces côté vendeur jusqu’à mise en vente.  
**Accès :** Création et gestion des annonces réservées au rôle SELLER ; consultation des annonces certifiées en lecture seule publique.

---

### 3.4 Certification des annonces (Certification)

- **Liste des annonces en attente** : annonces au statut « PENDING_CERTIFICATION ».
- **Certifier une annonce** : passage au statut « CERTIFIED » (visible sur le marché) ; déclenchement d’un événement métier → email au vendeur « Votre billet est certifié ».
- **Liste d’attente** : si l’événement a des abonnés en waitlist, envoi d’un email « Des billets sont disponibles » aux inscrits.

**Périmètre :** Validation métier des annonces avant mise en vente et notification (vendeur + waitlist).  
**Accès :** Réservé au rôle CONTROLLER.

---

### 3.5 Commandes et paiements (Orders / Sales)

- **Créer une commande** : à partir d’un listing certifié (listingId, buyerId) ; calcul du montant acheteur (prix + commission plateforme) ; statut initial « PENDING_PAYMENT » ; déclenchement email « Commande enregistrée » à l’acheteur.
- **Consulter une commande** : détail d’une commande (identifiant, statut, pricing : prix billet, commission, total).
- **Marquer une commande comme payée** : passage au statut « PAID » (après réception du paiement) ; déclenchement email « Paiement confirmé » au vendeur (avec détail vendeur + commission plateforme).

**Périmètre :** Cycle de vie de la commande (création → paiement → confirmation).  
**Accès :** Création et consultation des commandes pour BUYER (et vendeur selon règles métier) ; « marquer payée » réservé au CONTROLLER.

---

### 3.6 Webhooks de paiement (Payments)

- **Webhook générique** : réception des callbacks du prestataire de paiement (token de sécurité) ; traitement du statut « PAID » pour confirmer la commande et déclencher les notifications.
- **Webhook Stripe** (si provider = Stripe) : vérification de la signature Stripe ; traitement des événements `payment_intent.succeeded` pour confirmer le paiement.

**Périmètre :** Intégration avec le prestataire de paiement (Stripe ou autre) pour mettre à jour le statut des commandes et déclencher les emails sans action manuelle.  
**Accès :** Endpoints appelés par le prestataire (authentification par token / signature).

---

### 3.7 Liste d’attente (Waitlist)

- **S’inscrire** : ajout d’un acheteur (userId) à la liste d’attente d’un événement (eventId).
- **Se désinscrire** : retrait de la liste d’attente (eventId + userId).

**Périmètre :** Gestion des inscriptions par événement ; les notifications « billets disponibles » sont déclenchées par le bloc Certification lors de la certification d’une annonce pour cet événement.  
**Accès :** Réservé au rôle BUYER.

---

### 3.8 Notifications (Notification)

- **Envoi d’emails** selon le type d’événement métier :  
  - Commande créée (ORDER_PLACED) → acheteur.  
  - Paiement confirmé (ORDER_PAID) → vendeur.  
  - Annonce certifiée (LISTING_CERTIFIED) → vendeur.  
  - Billets disponibles (WAITLIST_TICKETS_AVAILABLE) → abonnés waitlist.
- **Templates** : contenu texte + HTML (marque Ticketio, liens vers l’app, infos contexte : événement, prix, montants).
- **Intégration** : envoi via prestataire (ex. Brevo) ; mode test avec faux envoi (FakeEmailSender) pour les tests automatisés.

**Périmètre :** Pas d’interface utilisateur dédiée ; les notifications sont déclenchées par les autres blocs (événements métier / observer).  
**Accès :** Interne (système).

---

## 4. Synthèse du découpage

| Bloc fonctionnel        | Acteurs principaux     | Rôle métier principal                          |
|--------------------------|------------------------|-----------------------------------------------|
| Auth                     | Tous                   | Identité, inscription, contrôle d’accès       |
| Catalog                  | Public / tous          | Référentiel d’événements (lecture)             |
| Listings                 | SELLER, public (lecture) | Annonces, pièces justificatives, visibilité  |
| Certification            | CONTROLLER             | Validation des annonces, notifications         |
| Orders / Sales           | BUYER, CONTROLLER      | Commandes, pricing, statut payé               |
| Payments (webhooks)      | Système / prestataire  | Réconciliation paiement → commande            |
| Waitlist                 | BUYER                  | Inscriptions / désinscriptions par événement  |
| Notification             | Système                | Emails (commande, paiement, certification, waitlist) |

Ce découpage permet de présenter le projet de manière claire en soutenance : **qui** a **quel besoin** et **quel bloc fonctionnel** y répond.
