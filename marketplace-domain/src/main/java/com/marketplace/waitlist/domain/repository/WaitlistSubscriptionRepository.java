package com.marketplace.waitlist.domain.repository;

import com.marketplace.waitlist.domain.model.WaitlistStatus;
import com.marketplace.waitlist.domain.model.WaitlistSubscription;

import java.util.List;
import java.util.Optional;

public interface WaitlistSubscriptionRepository {

    WaitlistSubscription save(WaitlistSubscription subscription);

    boolean existsByEventIdAndUserId(String eventId, String userId);

    List<WaitlistSubscription> findByEventId(String eventId);

    boolean deleteByEventIdAndUserId(String eventId, String userId);

    /** Retourne le premier inscrit WAITING par ordre d'inscription (FIFO). */
    Optional<WaitlistSubscription> findFirstWaitingByEventId(String eventId);

    /** Retourne toutes les souscriptions NOTIFIED dont la fenêtre a expiré. */
    List<WaitlistSubscription> findExpiredNotifications(int windowMinutes);

    Optional<WaitlistSubscription> findById(String id);

    List<WaitlistSubscription> findByEventIdAndStatus(String eventId, WaitlistStatus status);
}
