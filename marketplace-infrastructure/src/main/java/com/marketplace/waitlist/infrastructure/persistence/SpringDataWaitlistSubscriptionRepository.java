package com.marketplace.waitlist.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SpringDataWaitlistSubscriptionRepository extends JpaRepository<WaitlistSubscriptionEntity, String> {
    boolean existsByEventIdAndUserId(String eventId, String userId);

    List<WaitlistSubscriptionEntity> findByEventId(String eventId);

    @Modifying
    @Transactional
    long deleteByEventIdAndUserId(String eventId, String userId);
}
