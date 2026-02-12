package com.marketplace.listing.domain.valueobject;

public record ExternalEventId(String value) {
    public ExternalEventId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ExternalEventId cannot be blank");
        }
    }
}
