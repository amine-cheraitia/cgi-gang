package com.marketplace.waitlist.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataWaitlistSubscriptionRepository extends JpaRepository<WaitlistSubscriptionEntity, String> {
    boolean existsByEventIdAndUserId(String eventId, String userId);

    List<WaitlistSubscriptionEntity> findByEventId(String eventId);
}
