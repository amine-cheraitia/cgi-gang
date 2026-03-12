package com.marketplace.notification.application.event;

import com.marketplace.shared.application.event.ApplicationEvent;

public record ListingCertifiedApplicationEvent(
    String listingId,
    String sellerId,
    String eventId,
    String price,
    String sellerPayoutEstimate,
    String platformRevenueEstimate
) implements ApplicationEvent {
}
