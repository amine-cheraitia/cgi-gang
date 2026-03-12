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
        NotificationPayloadValidator.requireKeys(command, "orderId");
        String orderId         = command.data().getOrDefault("orderId", "N/A");
        String sellerPayout    = command.data().getOrDefault("sellerPayout", "");
        String platformRevenue = command.data().getOrDefault("platformRevenue", "");
        String event           = command.data().getOrDefault("eventName", "");
        String buyerTotal      = command.data().getOrDefault("buyerTotal", "");
        String role            = command.data().getOrDefault("role", "BUYER");

        boolean isSeller = "SELLER".equalsIgnoreCase(role);

        String subject = isSeller
            ? "Paiement confirme \u2705 – votre billet est vendu !"
            : "Paiement confirme \u2705 – votre billet est a vous !";

        StringBuilder text = new StringBuilder()
            .append("Bonjour ").append(command.recipientName()).append(",\n\n")
            .append("Le paiement de la commande ").append(orderId).append(" a ete confirme avec succes.\n");
        if (!event.isBlank()) {
            text.append("Evenement : ").append(event).append("\n");
        }
        if (!buyerTotal.isBlank()) {
            text.append("Montant paye : ").append(buyerTotal).append("\n");
        }
        if (!sellerPayout.isBlank()) {
            text.append("Net vendeur : ").append(sellerPayout).append("\n");
        }
        if (!platformRevenue.isBlank()) {
            text.append("Revenu plateforme : ").append(platformRevenue).append("\n");
        }
        text.append("\nMerci d'utiliser Ticketio !\n\nL'equipe Ticketio");

        String textBody = text.toString();

        String eventLine = event.isBlank() ? "" :
            "<p style=\"margin:0 0 4px;\">🎫 Evenement : <strong>" + EmailHtmlLayout.escape(event) + "</strong></p>";

        String buttonLabel = isSeller ? "Voir ma commande" : "Voir mon billet";
        String buttonUrl = isSeller
            ? "https://app.ticketio.com/orders/" + orderId
            : "https://app.ticketio.com/orders/" + orderId + "/ticket";

        String htmlBody = EmailHtmlLayout.wrap(
            subject,
            "<p>Bonjour <strong>" + EmailHtmlLayout.escape(command.recipientName()) + "</strong>,</p>"
            + "<p>\uD83C\uDF89 Le paiement de la commande a ete confirme.</p>"
            + eventLine
            + EmailHtmlLayout.infoTable(
                EmailHtmlLayout.infoRow("N° commande", orderId),
                buyerTotal.isBlank() ? null : EmailHtmlLayout.infoRow("Montant paye", buyerTotal),
                sellerPayout.isBlank() ? null : EmailHtmlLayout.infoRow("Net vendeur", sellerPayout),
                platformRevenue.isBlank() ? null : EmailHtmlLayout.infoRow("Revenu plateforme", platformRevenue),
                EmailHtmlLayout.infoRow("Statut", "PAYE")
              )
            + "<p style=\"margin-top:20px;color:#6b7280;font-size:13px;\">"
            + (isSeller
                ? "Conservez cet email comme preuve de vente et de reglement."
                : "Conservez cet email comme preuve d'achat. Vous pouvez recuperer votre billet via le lien ci-dessous.")
            + "</p>",
            buttonLabel,
            buttonUrl
        );

        return new EmailMessage(subject, textBody, htmlBody);
    }
}
