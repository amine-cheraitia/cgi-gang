package com.marketplace.catalog.domain.model;

import java.time.Instant;

public record ExternalEvent(
    String id,
    String name,
    Instant date,
    String venue,
    String city
) {
    public ExternalEvent {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event id is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Event name is required");
        }
    }
}
