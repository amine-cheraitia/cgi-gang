package com.marketplace.notification.application.model;

import java.util.Map;

public record NotificationCommand(
    String to,
    String recipientName,
    NotificationEventType eventType,
    Map<String, String> data
) {
}
