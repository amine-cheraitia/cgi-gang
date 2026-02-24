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
        String eventName     = command.data().getOrDefault("eventName", "cet evenement");
        String startingPrice = command.data().getOrDefault("startingPrice", "N/A");
        String eventDate     = command.data().getOrDefault("eventDate", "");
        String venue         = command.data().getOrDefault("venue", "");

        String subject = "\uD83D\uDD14 Des billets sont disponibles pour " + eventName + " !";

        String textBody = "Bonjour " + command.recipientName() + ",\n\n"
            + "Bonne nouvelle ! Des billets sont de nouveau disponibles pour " + eventName + ".\n"
            + (eventDate.isBlank() ? "" : "Date : " + eventDate + "\n")
            + (venue.isBlank()     ? "" : "Lieu : " + venue + "\n")
            + "Prix a partir de : " + startingPrice + "\n\n"
            + "Depechez-vous, les billets partent vite !\n\nL'equipe MiamCampus";

        String dateRow  = eventDate.isBlank() ? "" : EmailHtmlLayout.infoRow("Date", eventDate);
        String venueRow = venue.isBlank()     ? "" : EmailHtmlLayout.infoRow("Lieu", venue);

        String htmlBody = EmailHtmlLayout.wrap(
            subject,
            "<p>Bonjour <strong>" + EmailHtmlLayout.escape(command.recipientName()) + "</strong>,</p>"
            + "<p>\uD83C\uDF89 Vous etes sur la liste d'attente et des billets viennent de se liberer !</p>"
            + EmailHtmlLayout.infoTable(
                EmailHtmlLayout.infoRow("Evenement", eventName),
                dateRow,
                venueRow,
                EmailHtmlLayout.infoRow("A partir de", startingPrice)
              )
            + "<p style=\"margin-top:20px;font-weight:600;color:#FF6B35;\">"
            + "⚡ Les billets partent vite — agissez maintenant !</p>"
            + "<p style=\"color:#6b7280;font-size:13px;\">"
            + "Vous recevez cet email car vous etes inscrit sur la liste d'attente pour cet evenement.</p>",
            "Voir les billets disponibles",
            "https://app.miamcampus.com/events"
        );

        return new EmailMessage(subject, textBody, htmlBody);
    }
}
