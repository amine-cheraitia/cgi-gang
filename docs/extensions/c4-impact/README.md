# Impact C4 previsionnel (Current -> Target)

## Intention

Montrer l'evolution architecturale sans regression des composants existants.

## Delta de composants

- Ajout `NotificationOrchestrator` (multi-canal) et `SmsSenderAdapter`.
- Ajout `PricingPolicyResolver` + strategies de pricing.
- Ajout `SecondCatalogProviderAdapter`.
- Ajout `MinioObjectStorageAdapter`.
- Ajout `CertificationRuleChain`.

## Diagramme C4 Component (target simplifie)

```mermaid
flowchart LR
  UI[REST Controllers]
  APP[Application Use Cases]
  DOM[Domain Model and Policies]

  NOTIF[Notification Orchestrator]
  EMAIL[EmailSender Adapter]
  SMS[SmsSender Adapter]

  PRICE[PricingPolicyResolver]
  POL1[StandardPolicy]
  POL2[PremiumPolicy]
  POL3[LastMinutePolicy]

  CATP2[SecondCatalogProviderAdapter]
  STO2[MinioObjectStorageAdapter]
  CERT[CertificationRuleChain]

  UI --> APP --> DOM

  APP --> NOTIF
  NOTIF --> EMAIL
  NOTIF --> SMS

  APP --> PRICE
  PRICE --> POL1
  PRICE --> POL2
  PRICE --> POL3

  APP --> CATP2
  APP --> STO2
  APP --> CERT
```

## Lecture barème

- Extension de comportement sans modification massive: **OCP**.
- Dependance sur des contrats metier: **DIP**.
- Responsabilites isolees par composant: **SRP**.
- Preuve de modularite et evolutivite conforme a l'attendu fil rouge.
