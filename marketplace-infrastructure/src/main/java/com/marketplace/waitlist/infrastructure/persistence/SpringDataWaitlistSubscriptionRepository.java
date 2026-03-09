package com.marketplace.waitlist.infrastructure.persistence;

import com.marketplace.waitlist.domain.model.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataWaitlistSubscriptionRepository
    extends JpaRepository<WaitlistSubscriptionEntity, String> {

    boolean existsByEventIdAndUserId(String eventId, String userId);

    List<WaitlistSubscriptionEntity> findByEventId(String eventId);

    List<WaitlistSubscriptionEntity> findByEventIdAndStatus(String eventId, WaitlistStatus status);

    @Modifying
    @Transactional
    long deleteByEventIdAndUserId(String eventId, String userId);

    /** Premier inscrit WAITING pour un événement, trié par date d'inscription (FIFO). */
    Optional<WaitlistSubscriptionEntity> findFirstByEventIdAndStatusOrderByCreatedAtAsc(
        String eventId, WaitlistStatus status
    );

    /** Souscriptions NOTIFIED dont la fenêtre a expiré. */
    @Query("SELECT w FROM WaitlistSubscriptionEntity w " +
           "WHERE w.status = 'NOTIFIED' AND w.notifiedAt < :expiryThreshold")
    List<WaitlistSubscriptionEntity> findExpiredNotifications(
        @Param("expiryThreshold") OffsetDateTime expiryThreshold
    );
}
