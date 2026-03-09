package com.marketplace.waitlist.application.usecase;

import com.marketplace.notification.application.event.WaitlistTicketsAvailableApplicationEvent;
import com.marketplace.shared.application.event.ApplicationEventDispatcher;
import com.marketplace.waitlist.domain.model.WaitlistSubscription;
import com.marketplace.waitlist.domain.repository.WaitlistSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Logique de passation FIFO :
 * - Expire les souscriptions NOTIFIED dont la fenêtre de 15 min est écoulée.
 * - Pour chaque expiration, notifie le prochain inscrit WAITING sur le même événement.
 */
@Service
public class PassWaitlistToNextUseCase {

    static final int NOTIFICATION_WINDOW_MINUTES = 15;

    private final WaitlistSubscriptionRepository waitlistRepository;
    private final ApplicationEventDispatcher eventDispatcher;

    public PassWaitlistToNextUseCase(WaitlistSubscriptionRepository waitlistRepository,
                                     ApplicationEventDispatcher eventDispatcher) {
        this.waitlistRepository = waitlistRepository;
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * Appelé par le scheduler toutes les minutes.
     * Pour chaque notification expirée, passe au suivant dans la file.
     */
    public void processExpiredNotifications() {
        List<WaitlistSubscription> expired =
            waitlistRepository.findExpiredNotifications(NOTIFICATION_WINDOW_MINUTES);

        for (WaitlistSubscription sub : expired) {
            sub.expire();
            waitlistRepository.save(sub);

            // Cherche le prochain WAITING pour cet événement
            Optional<WaitlistSubscription> nextInLine =
                waitlistRepository.findFirstWaitingByEventId(sub.getEventId());

            nextInLine.ifPresent(next -> {
                next.markNotified();
                waitlistRepository.save(next);
                eventDispatcher.dispatch(new WaitlistTicketsAvailableApplicationEvent(
                    next.getEventId(),
                    next.getUserId(),
                    "N/A"   // le prix sera résolu dans le handler via le catalogue
                ));
            });
        }
    }
}
