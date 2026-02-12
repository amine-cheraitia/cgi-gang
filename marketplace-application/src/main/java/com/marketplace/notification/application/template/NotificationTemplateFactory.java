package com.marketplace.notification.application.template;

import com.marketplace.notification.application.model.NotificationEventType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationTemplateFactory {
    private final List<EmailTemplateStrategy> strategies;

    public NotificationTemplateFactory(List<EmailTemplateStrategy> strategies) {
        this.strategies = strategies;
    }

    public EmailTemplateStrategy resolve(NotificationEventType eventType) {
        return strategies.stream()
            .filter(strategy -> strategy.supports(eventType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No template strategy for " + eventType));
    }
}
