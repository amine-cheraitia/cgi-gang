package com.marketplace.waitlist.domain.repository;

import com.marketplace.waitlist.domain.model.WaitlistSubscription;

import java.util.List;

public interface WaitlistSubscriptionRepository {
    WaitlistSubscription save(WaitlistSubscription subscription);

    boolean existsByEventIdAndUserId(String eventId, String userId);

    List<WaitlistSubscription> findByEventId(String eventId);
}
