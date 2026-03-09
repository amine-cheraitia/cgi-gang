package com.marketplace.waitlist.infrastructure.scheduler;

import com.marketplace.waitlist.application.usecase.PassWaitlistToNextUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler FIFO : vérifie toutes les minutes les notifications expirées
 * et passe au suivant dans la file d'attente.
 */
@Component
public class WaitlistExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(WaitlistExpirationScheduler.class);

    private final PassWaitlistToNextUseCase passToNextUseCase;

    public WaitlistExpirationScheduler(PassWaitlistToNextUseCase passToNextUseCase) {
        this.passToNextUseCase = passToNextUseCase;
    }

    @Scheduled(fixedDelay = 60_000)   // toutes les 60 secondes
    public void processExpiredNotifications() {
        log.debug("[WAITLIST] Vérification des notifications expirées...");
        try {
            passToNextUseCase.processExpiredNotifications();
        } catch (Exception e) {
            log.error("[WAITLIST] Erreur lors de la passation à l'inscrit suivant", e);
        }
    }
}
