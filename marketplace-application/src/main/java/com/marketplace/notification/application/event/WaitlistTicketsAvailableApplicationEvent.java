package com.marketplace.notification.application.event;

import com.marketplace.shared.application.event.ApplicationEvent;

public record WaitlistTicketsAvailableApplicationEvent(
    String eventId,
    String targetUserId,   // FIFO : un seul utilisateur notifié à la fois
    String startingPrice
) implements ApplicationEvent {
}
