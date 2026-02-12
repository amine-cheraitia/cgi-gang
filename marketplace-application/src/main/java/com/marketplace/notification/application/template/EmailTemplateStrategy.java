package com.marketplace.notification.application.template;

import com.marketplace.notification.application.model.EmailMessage;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;

public interface EmailTemplateStrategy {
    boolean supports(NotificationEventType eventType);

    EmailMessage build(NotificationCommand command);
}
