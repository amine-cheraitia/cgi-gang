package com.marketplace.notification.application.template;

import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;

import java.util.Arrays;

final class NotificationPayloadValidator {
    private NotificationPayloadValidator() {
    }

    static void requireKeys(NotificationCommand command, String... keys) {
        for (String key : keys) {
            String value = command.data().get(key);
            if (value == null || value.isBlank()) {
                throw new BusinessException(
                    ErrorCode.NOTIFICATION_TEMPLATE_PAYLOAD_INVALID,
                    "Missing notification payload key: " + key + " for " + command.eventType()
                );
            }
        }
    }

    static void requireAnyKey(NotificationCommand command, String... keys) {
        boolean present = Arrays.stream(keys)
            .map(command.data()::get)
            .anyMatch(value -> value != null && !value.isBlank());
        if (!present) {
            throw new BusinessException(
                ErrorCode.NOTIFICATION_TEMPLATE_PAYLOAD_INVALID,
                "Missing notification payload keys: " + Arrays.toString(keys) + " for " + command.eventType()
            );
        }
    }
}
