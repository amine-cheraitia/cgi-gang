package com.marketplace.catalog.infrastructure.rest.dto;

import com.marketplace.catalog.domain.model.ExternalEvent;

import java.time.Instant;

public record EventResponse(
    String id,
    String name,
    Instant date,
    String venue,
    String city,
    int availableListings
) {
    public static EventResponse from(ExternalEvent event, int availableListings) {
        return new EventResponse(event.id(), event.name(), event.date(), event.venue(), event.city(), availableListings);
    }
}
