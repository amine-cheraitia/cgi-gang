package com.marketplace.notification.application.event;

import com.marketplace.shared.application.event.ApplicationEvent;

public record ListingPendingReviewApplicationEvent(
        String listingId,
        String eventId
) implements ApplicationEvent {
}

