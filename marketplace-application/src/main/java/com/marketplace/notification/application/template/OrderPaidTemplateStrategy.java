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
        String orderId = command.data().getOrDefault("orderId", "N/A");
        String sellerPayout = command.data().getOrDefault("sellerPayout", "N/A");
        String platformRevenue = command.data().getOrDefault("platformRevenue", "N/A");
        String subject = "Paiement confirme";
        String textBody = "Bonjour " + command.recipientName() + ",\n\n"
            + "Le paiement de la commande " + orderId + " est confirme.\n"
            + "Net vendeur: " + sellerPayout + ". Revenu plateforme: " + platformRevenue + ".\n";
        String htmlBody = EmailHtmlLayout.wrap(subject,
            "<p>Bonjour <strong>" + EmailHtmlLayout.escape(command.recipientName()) + "</strong>,</p>"
                + "<p>Le paiement de la commande <strong>" + EmailHtmlLayout.escape(orderId) + "</strong> est confirme.</p>"
                + "<p>Net vendeur: <strong>" + EmailHtmlLayout.escape(sellerPayout)
                + "</strong><br/>Revenu plateforme: <strong>" + EmailHtmlLayout.escape(platformRevenue) + "</strong></p>");
        return new EmailMessage(subject, textBody, htmlBody);
    }
}
