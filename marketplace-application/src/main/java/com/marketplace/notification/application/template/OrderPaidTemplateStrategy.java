package com.marketplace.notification.application.template;

import com.marketplace.notification.application.model.EmailMessage;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;
import org.springframework.stereotype.Component;

@Component
public class OrderPaidTemplateStrategy implements EmailTemplateStrategy {
    @Override
    public boolean supports(NotificationEventType eventType) {
        return eventType == NotificationEventType.ORDER_PAID;
    }

    @Override
    public EmailMessage build(NotificationCommand command) {
        NotificationPayloadValidator.requireKeys(command, "orderId", "sellerPayout", "platformRevenue");
        String orderId         = command.data().getOrDefault("orderId", "N/A");
        String sellerPayout    = command.data().getOrDefault("sellerPayout", "N/A");
        String platformRevenue = command.data().getOrDefault("platformRevenue", "N/A");
        String event           = command.data().getOrDefault("eventName", "");
        String buyerTotal      = command.data().getOrDefault("buyerTotal", "N/A");

        String subject = "Paiement confirme \u2705 – votre billet est a vous !";

        String textBody = "Bonjour " + command.recipientName() + ",\n\n"
            + "Le paiement de la commande " + orderId + " a ete confirme avec succes.\n"
            + (event.isBlank() ? "" : "Evenement : " + event + "\n")
            + "Montant paye : " + buyerTotal + "\n"
            + "Net vendeur : " + sellerPayout + "\n\n"
            + "Merci d'utiliser MiamCampus !\n\nL'equipe MiamCampus";

        String eventLine = event.isBlank() ? "" :
            "<p style=\"margin:0 0 4px;\">🎫 Evenement : <strong>" + EmailHtmlLayout.escape(event) + "</strong></p>";

        String htmlBody = EmailHtmlLayout.wrap(
            subject,
            "<p>Bonjour <strong>" + EmailHtmlLayout.escape(command.recipientName()) + "</strong>,</p>"
            + "<p>\uD83C\uDF89 Votre paiement a ete confirme ! Votre billet est desormais reserve.</p>"
            + eventLine
            + EmailHtmlLayout.infoTable(
                EmailHtmlLayout.infoRow("N° commande", orderId),
                EmailHtmlLayout.infoRow("Montant paye", buyerTotal),
                EmailHtmlLayout.infoRow("Net vendeur", sellerPayout),
                EmailHtmlLayout.infoRow("Revenu plateforme", platformRevenue),
                EmailHtmlLayout.infoRow("Statut", "PAYE")
              )
            + "<p style=\"margin-top:20px;color:#6b7280;font-size:13px;\">"
            + "Conservez cet email comme preuve d'achat. Presentez votre billet le jour J."
            + "</p>",
            "Voir ma commande",
            "https://app.miamcampus.com/orders/" + orderId
        );

        return new EmailMessage(subject, textBody, htmlBody);
    }
}
