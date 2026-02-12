package com.marketplace.notification.application.event;

import com.marketplace.shared.application.event.ApplicationEvent;

public record OrderPlacedApplicationEvent(
    String orderId,
    String buyerId,
    String buyerTotal
) implements ApplicationEvent {
}
