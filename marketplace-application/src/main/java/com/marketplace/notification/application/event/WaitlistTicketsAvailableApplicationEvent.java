package com.marketplace.notification.application.event;

import com.marketplace.shared.application.event.ApplicationEvent;

public record WaitlistTicketsAvailableApplicationEvent(
    String eventId,
    String startingPrice
) implements ApplicationEvent {
}
