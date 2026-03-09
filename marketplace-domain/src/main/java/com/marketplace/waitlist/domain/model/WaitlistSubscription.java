package com.marketplace.waitlist.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class WaitlistSubscription {
    private final String id;
    private final String eventId;
    private final String userId;
    private final OffsetDateTime createdAt;
    private WaitlistStatus status;
    private OffsetDateTime notifiedAt;

    private WaitlistSubscription(String id, String eventId, String userId,
                                  OffsetDateTime createdAt, WaitlistStatus status,
                                  OffsetDateTime notifiedAt) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.status = status;
        this.notifiedAt = notifiedAt;
    }

    public static WaitlistSubscription create(String eventId, String userId) {
        return new WaitlistSubscription(
            UUID.randomUUID().toString(), eventId, userId,
            OffsetDateTime.now(), WaitlistStatus.WAITING, null
        );
    }

    public static WaitlistSubscription rehydrate(String id, String eventId, String userId,
                                                  OffsetDateTime createdAt, WaitlistStatus status,
                                                  OffsetDateTime notifiedAt) {
        return new WaitlistSubscription(id, eventId, userId, createdAt, status, notifiedAt);
    }

    /** Notifie cet inscrit : passe en NOTIFIED avec timestamp. */
    public void markNotified() {
        if (this.status != WaitlistStatus.WAITING) {
            throw new IllegalStateException("Seul un inscrit WAITING peut être notifié");
        }
        this.status = WaitlistStatus.NOTIFIED;
        this.notifiedAt = OffsetDateTime.now();
    }

    /** Expire la notification : fenêtre écoulée, passe au suivant. */
    public void expire() {
        if (this.status != WaitlistStatus.NOTIFIED) {
            throw new IllegalStateException("Seul un inscrit NOTIFIED peut expirer");
        }
        this.status = WaitlistStatus.EXPIRED;
    }

    /** Marque l'achat effectué. */
    public void markPurchased() {
        this.status = WaitlistStatus.PURCHASED;
    }

    public boolean isExpired(int windowMinutes) {
        if (this.status != WaitlistStatus.NOTIFIED || this.notifiedAt == null) return false;
        return OffsetDateTime.now().isAfter(notifiedAt.plusMinutes(windowMinutes));
    }

    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public String getUserId() { return userId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public WaitlistStatus getStatus() { return status; }
    public OffsetDateTime getNotifiedAt() { return notifiedAt; }
}
