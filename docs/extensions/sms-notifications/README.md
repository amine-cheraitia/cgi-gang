# Extension: Notifications SMS

## Objectif

Ajouter un canal SMS en plus de l'email pour les evenements metier critiques (`ORDER_PAID`, `LISTING_CERTIFIED`, `WAITLIST_TICKETS_AVAILABLE`).

## SOLID et Patterns

- **OCP**: ajout d'un nouveau canal sans modifier les cas d'usage existants.
- **DIP**: dependance vers une abstraction `NotificationSender`.
- **SRP**: separation entre rendu de template et transport du message.
- **Patterns**: `Strategy` (template par canal), `Factory` (selection du sender), `Adapter` (provider SMS externe).

## Impact architecture

- **Domain**: aucun changement de regles metier.
- **Application**: orchestration multi-canal selon preference utilisateur.
- **Infrastructure**: nouvel adapter `SmsSender` (fake en test/dev, provider reel ensuite).

## Tests cibles

- TU sur selection de canal et rendu template SMS.
- TI: verification qu'un event metier declenche email seul ou email+sms selon profil.
- Contrat erreur stable si provider SMS indisponible (fallback email).
