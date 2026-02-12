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
        String orderId = command.data().getOrDefault("orderId", "N/A");
        String sellerPayout = command.data().getOrDefault("sellerPayout", "N/A");
        String platformRevenue = command.data().getOrDefault("platformRevenue", "N/A");
        String subject = "Paiement confirme";
        String body = "Bonjour " + command.recipientName() + ",\n\n"
            + "Le paiement de la commande " + orderId + " est confirme.\n"
            + "Net vendeur: " + sellerPayout + ". Revenu plateforme: " + platformRevenue + ".\n";
        return new EmailMessage(subject, body);
    }
}
