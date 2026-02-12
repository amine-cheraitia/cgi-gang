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
        NotificationPayloadValidator.requireAnyKey(command, "eventName", "listingId");
        String eventName = command.data().getOrDefault("eventName", "votre evenement");
        String subject = "Votre billet est certifie";
        String textBody = "Bonjour " + command.recipientName() + ",\n\n"
            + "Votre billet pour " + eventName + " est maintenant certifie et visible sur la marketplace.\n";
        String htmlBody = EmailHtmlLayout.wrap(subject,
            "<p>Bonjour <strong>" + EmailHtmlLayout.escape(command.recipientName()) + "</strong>,</p>"
                + "<p>Votre billet pour <strong>" + EmailHtmlLayout.escape(eventName)
                + "</strong> est maintenant certifie et visible sur la marketplace.</p>");
        return new EmailMessage(subject, textBody, htmlBody);
    }
}
