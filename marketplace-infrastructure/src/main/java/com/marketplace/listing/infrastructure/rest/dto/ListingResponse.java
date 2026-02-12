package com.marketplace.listing.infrastructure.rest.dto;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.model.ListingStatus;

import java.math.BigDecimal;

public record ListingResponse(
    String id,
    String eventId,
    String sellerId,
    BigDecimal price,
    String currency,
    ListingStatus status
) {
    public static ListingResponse from(Listing listing) {
        return new ListingResponse(
            listing.getId(),
            listing.getExternalEventId().value(),
            listing.getSellerId(),
            listing.getPrice().amount(),
            listing.getPrice().currency().getCurrencyCode(),
            listing.getStatus()
        );
    }
}
