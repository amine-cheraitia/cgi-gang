package com.marketplace.waitlist.infrastructure.rest.dto;

import com.marketplace.waitlist.domain.model.WaitlistSubscription;

import java.time.OffsetDateTime;

public record WaitlistSubscriptionResponse(
    String id,
    String eventId,
    String userId,
    OffsetDateTime createdAt
) {
    public static WaitlistSubscriptionResponse from(WaitlistSubscription subscription) {
        return new WaitlistSubscriptionResponse(
            subscription.getId(),
            subscription.getEventId(),
            subscription.getUserId(),
            subscription.getCreatedAt()
        );
    }
}
