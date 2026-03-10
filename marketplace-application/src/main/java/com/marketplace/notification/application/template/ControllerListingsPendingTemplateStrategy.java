package com.marketplace.notification.application.template;

import com.marketplace.notification.application.model.EmailMessage;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;
import org.springframework.stereotype.Component;

@Component
public class ControllerListingsPendingTemplateStrategy implements EmailTemplateStrategy {

    @Override
    public boolean supports(NotificationEventType eventType) {
        return eventType == NotificationEventType.CONTROLLER_LISTINGS_PENDING;
    }

    @Override
    public EmailMessage build(NotificationCommand command) {
        NotificationPayloadValidator.requireAnyKey(command, "eventId", "listingId");
        String eventId = command.data().getOrDefault("eventId", "evenement inconnu");
        String listingId = command.data().getOrDefault("listingId", "N/A");

        String subject = "Nouvelles annonces à certifier";

        String textBody = "Bonjour " + command.recipientName() + ",\n\n"
                + "Une nouvelle annonce est en attente de certification.\n"
                + "Evenement : " + eventId + "\n"
                + "Listing : " + listingId + "\n\n"
                + "Merci de vous connecter à l'espace contrôleur pour la traiter.\n";

        String htmlBody = EmailHtmlLayout.wrap(
                subject,
                "<p>Bonjour <strong>" + EmailHtmlLayout.escape(command.recipientName()) + "</strong>,</p>"
                        + "<p>Une nouvelle annonce est <strong>en attente de certification</strong>.</p>"
                        + EmailHtmlLayout.infoTable(
                                EmailHtmlLayout.infoRow("Événement", eventId),
                                EmailHtmlLayout.infoRow("Listing", listingId)
                        )
                        + "<p style=\"margin-top:20px;color:#6b7280;font-size:13px;\">"
                        + "Connectez-vous à l'espace contrôleur pour examiner et certifier cette annonce."
                        + "</p>",
                "Voir les annonces à certifier",
                "https://app.ticketio.com/admin/certifications/pending"
        );

        return new EmailMessage(subject, textBody, htmlBody);
    }
}

