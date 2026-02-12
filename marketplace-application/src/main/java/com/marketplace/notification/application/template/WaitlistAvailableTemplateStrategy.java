package com.marketplace.notification.application.template;

import com.marketplace.notification.application.model.EmailMessage;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;
import org.springframework.stereotype.Component;

@Component
public class WaitlistAvailableTemplateStrategy implements EmailTemplateStrategy {
    @Override
    public boolean supports(NotificationEventType eventType) {
        return eventType == NotificationEventType.WAITLIST_TICKETS_AVAILABLE;
    }

    @Override
    public EmailMessage build(NotificationCommand command) {
        NotificationPayloadValidator.requireKeys(command, "eventName", "startingPrice");
        String eventName = command.data().getOrDefault("eventName", "cet evenement");
        String startingPrice = command.data().getOrDefault("startingPrice", "N/A");
        String subject = "Billets disponibles";
        String textBody = "Bonjour " + command.recipientName() + ",\n\n"
            + "Des billets sont de nouveau disponibles pour " + eventName + ".\n"
            + "Prix a partir de " + startingPrice + ".\n";
        String htmlBody = EmailHtmlLayout.wrap(subject,
            "<p>Bonjour <strong>" + EmailHtmlLayout.escape(command.recipientName()) + "</strong>,</p>"
                + "<p>Des billets sont de nouveau disponibles pour <strong>" + EmailHtmlLayout.escape(eventName) + "</strong>.</p>"
                + "<p>Prix a partir de <strong>" + EmailHtmlLayout.escape(startingPrice) + "</strong>.</p>");
        return new EmailMessage(subject, textBody, htmlBody);
    }
}
