package com.marketplace.waitlist.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class WaitlistSubscription {
    private final String id;
    private final String eventId;
    private final String userId;
    private final OffsetDateTime createdAt;

    private WaitlistSubscription(String id, String eventId, String userId, OffsetDateTime createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static WaitlistSubscription create(String eventId, String userId) {
        return new WaitlistSubscription(UUID.randomUUID().toString(), eventId, userId, OffsetDateTime.now());
    }

    public static WaitlistSubscription rehydrate(String id, String eventId, String userId, OffsetDateTime createdAt) {
        return new WaitlistSubscription(id, eventId, userId, createdAt);
    }

    public String getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getUserId() {
        return userId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
