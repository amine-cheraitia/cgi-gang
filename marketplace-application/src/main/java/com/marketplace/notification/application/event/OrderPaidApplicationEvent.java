package com.marketplace.notification.application.event;

import com.marketplace.shared.application.event.ApplicationEvent;

public record OrderPaidApplicationEvent(
    String orderId,
    String buyerId,
    String sellerId,
    String sellerPayout,
    String platformRevenue
) implements ApplicationEvent {
}
