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
        String eventName  = command.data().getOrDefault("eventName", "votre evenement");
        String listingId  = command.data().getOrDefault("listingId", "N/A");
        String price      = command.data().getOrDefault("price", "");

        String subject = "Votre billet est certifie \uD83D\uDD16 et en vente !";

        String textBody = "Bonjour " + command.recipientName() + ",\n\n"
            + "Bonne nouvelle ! Votre billet pour " + eventName + " a ete certifie.\n"
            + "Il est maintenant visible sur MiamCampus et peut etre achete par des acheteurs.\n"
            + (price.isBlank() ? "" : "Prix de vente : " + price + "\n")
            + "\nL'equipe MiamCampus";

        String priceRow = price.isBlank() ? "" : EmailHtmlLayout.infoRow("Prix de vente", price);

        String htmlBody = EmailHtmlLayout.wrap(
            subject,
            "<p>Bonjour <strong>" + EmailHtmlLayout.escape(command.recipientName()) + "</strong>,</p>"
            + "<p>\uD83C\uDF1F Bonne nouvelle ! Votre billet a ete verifie et certifie par notre equipe.</p>"
            + EmailHtmlLayout.infoTable(
                EmailHtmlLayout.infoRow("Evenement", eventName),
                EmailHtmlLayout.infoRow("Reference listing", listingId),
                priceRow,
                EmailHtmlLayout.infoRow("Statut", "CERTIFIE – en vente")
              )
            + "<p style=\"margin-top:20px;color:#6b7280;font-size:13px;\">"
            + "Votre billet est desormais visible par tous les acheteurs sur la marketplace. "
            + "Vous recevrez une notification des qu'il sera vendu.</p>",
            "Voir mon annonce",
            "https://app.miamcampus.com/listings/" + listingId
        );

        return new EmailMessage(subject, textBody, htmlBody);
    }
}
