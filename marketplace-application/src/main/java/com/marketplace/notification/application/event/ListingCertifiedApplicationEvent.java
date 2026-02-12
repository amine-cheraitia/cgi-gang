package com.marketplace.notification.application.event;

import com.marketplace.shared.application.event.ApplicationEvent;

public record ListingCertifiedApplicationEvent(
    String listingId,
    String sellerId,
    String eventId
) implements ApplicationEvent {
}
