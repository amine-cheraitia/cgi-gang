package com.marketplace.notification.application.template;

import com.marketplace.notification.application.model.EmailMessage;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;
import org.springframework.stereotype.Component;

@Component
public class ListingCertifiedTemplateStrategy implements EmailTemplateStrategy {
    @Override
    public boolean supports(NotificationEventType eventType) {
        return eventType == NotificationEventType.LISTING_CERTIFIED;
    }

    @Override
    public EmailMessage build(NotificationCommand command) {
        String eventName = command.data().getOrDefault("eventName", "votre evenement");
        String subject = "Votre billet est certifie";
        String body = "Bonjour " + command.recipientName() + ",\n\n"
            + "Votre billet pour " + eventName + " est maintenant certifie et visible sur la marketplace.\n";
        return new EmailMessage(subject, body);
    }
}
