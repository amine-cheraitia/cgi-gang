package com.marketplace.notification.application.template;

import com.marketplace.notification.application.model.EmailMessage;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedTemplateStrategy implements EmailTemplateStrategy {
    @Override
    public boolean supports(NotificationEventType eventType) {
        return eventType == NotificationEventType.ORDER_PLACED;
    }

    @Override
    public EmailMessage build(NotificationCommand command) {
        NotificationPayloadValidator.requireKeys(command, "orderId", "buyerTotal");
        String total   = command.data().getOrDefault("buyerTotal", "N/A");
        String orderId = command.data().getOrDefault("orderId", "N/A");
        String event   = command.data().getOrDefault("eventName", "");

        String subject = "Votre commande a bien ete enregistree \uD83C\uDFAB";

        String textBody = "Bonjour " + command.recipientName() + ",\n\n"
            + "Votre commande " + orderId + " a ete enregistree avec succes.\n"
            + (event.isBlank() ? "" : "Evenement : " + event + "\n")
            + "Montant total : " + total + "\n\n"
            + "Finalisez votre paiement pour confirmer la reservation.\n\n"
            + "L'equipe Ticketio";

        String eventLine = event.isBlank() ? "" :
            "<p style=\"margin:0 0 8px;\">🎫 Evenement : <strong>" + EmailHtmlLayout.escape(event) + "</strong></p>";

        String htmlBody = EmailHtmlLayout.wrap(
            subject,
            "<p>Bonjour <strong>" + EmailHtmlLayout.escape(command.recipientName()) + "</strong>,</p>"
            + "<p>Votre commande a bien ete enregistree sur Ticketio.</p>"
            + eventLine
            + EmailHtmlLayout.infoTable(
                EmailHtmlLayout.infoRow("N° commande", orderId),
                EmailHtmlLayout.infoRow("Montant total", total),
                EmailHtmlLayout.infoRow("Statut", "En attente de paiement")
              )
            + "<p style=\"margin-top:20px;color:#6b7280;font-size:13px;\">"
            + "Finalisez votre paiement pour confirmer votre billet. "
            + "Si vous n'etes pas a l'origine de cette commande, ignorez cet email.</p>",
            "Finaliser le paiement",
            "https://app.ticketio.com/orders/" + orderId
        );

        return new EmailMessage(subject, textBody, htmlBody);
    }
}
