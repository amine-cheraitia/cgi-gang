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
        String total = command.data().getOrDefault("buyerTotal", "N/A");
        String orderId = command.data().getOrDefault("orderId", "N/A");
        String subject = "Commande creee";
        String body = "Bonjour " + command.recipientName() + ",\n\n"
            + "Votre commande " + orderId + " est creee.\n"
            + "Montant total a payer: " + total + ".\n";
        return new EmailMessage(subject, body);
    }
}
