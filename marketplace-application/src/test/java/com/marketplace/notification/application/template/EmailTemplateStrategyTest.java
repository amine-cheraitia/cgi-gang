package com.marketplace.notification.application.template;

import com.marketplace.notification.application.model.EmailMessage;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTemplateStrategyTest {

    @Test
    void listingCertifiedTemplateShouldInjectPlaceholders() {
        ListingCertifiedTemplateStrategy strategy = new ListingCertifiedTemplateStrategy();
        NotificationCommand command = new NotificationCommand(
            "seller@marketplace.local",
            "seller",
            NotificationEventType.LISTING_CERTIFIED,
            Map.of("eventName", "Taylor Swift")
        );

        EmailMessage message = strategy.build(command);

        assertThat(message.subject()).isEqualTo("Votre billet est certifie");
        assertThat(message.body()).contains("seller");
        assertThat(message.body()).contains("Taylor Swift");
        assertThat(message.htmlBody()).contains("<html>");
        assertThat(message.htmlBody()).contains("Taylor Swift");
    }

    @Test
    void orderPlacedTemplateShouldInjectPlaceholders() {
        OrderPlacedTemplateStrategy strategy = new OrderPlacedTemplateStrategy();
        NotificationCommand command = new NotificationCommand(
            "buyer@marketplace.local",
            "buyer",
            NotificationEventType.ORDER_PLACED,
            Map.of("orderId", "ord_123", "buyerTotal", "90.00 EUR")
        );

        EmailMessage message = strategy.build(command);

        assertThat(message.subject()).isEqualTo("Commande creee");
        assertThat(message.body()).contains("ord_123");
        assertThat(message.body()).contains("90.00 EUR");
        assertThat(message.htmlBody()).contains("ord_123");
    }

    @Test
    void waitlistTemplateShouldInjectPlaceholders() {
        WaitlistAvailableTemplateStrategy strategy = new WaitlistAvailableTemplateStrategy();
        NotificationCommand command = new NotificationCommand(
            "user@marketplace.local",
            "user",
            NotificationEventType.WAITLIST_TICKETS_AVAILABLE,
            Map.of("eventName", "PSG vs OM", "startingPrice", "45.00 EUR")
        );

        EmailMessage message = strategy.build(command);

        assertThat(message.subject()).isEqualTo("Billets disponibles");
        assertThat(message.body()).contains("PSG vs OM");
        assertThat(message.body()).contains("45.00 EUR");
        assertThat(message.htmlBody()).contains("PSG vs OM");
    }

    @Test
    void orderPaidTemplateShouldInjectPlaceholders() {
        OrderPaidTemplateStrategy strategy = new OrderPaidTemplateStrategy();
        NotificationCommand command = new NotificationCommand(
            "seller@marketplace.local",
            "seller",
            NotificationEventType.ORDER_PAID,
            Map.of("orderId", "ord_456", "sellerPayout", "76.00 EUR", "platformRevenue", "14.00 EUR")
        );

        EmailMessage message = strategy.build(command);

        assertThat(message.subject()).isEqualTo("Paiement confirme");
        assertThat(message.body()).contains("ord_456");
        assertThat(message.body()).contains("76.00 EUR");
        assertThat(message.body()).contains("14.00 EUR");
        assertThat(message.htmlBody()).contains("ord_456");
    }

    @Test
    void orderPlacedTemplateShouldRejectMissingPayload() {
        OrderPlacedTemplateStrategy strategy = new OrderPlacedTemplateStrategy();
        NotificationCommand command = new NotificationCommand(
            "buyer@marketplace.local",
            "buyer",
            NotificationEventType.ORDER_PLACED,
            Map.of("orderId", "ord_123")
        );

        assertThatThrownBy(() -> strategy.build(command))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.NOTIFICATION_TEMPLATE_PAYLOAD_INVALID);
    }

    @Test
    void waitlistTemplateShouldRejectMissingPayload() {
        WaitlistAvailableTemplateStrategy strategy = new WaitlistAvailableTemplateStrategy();
        NotificationCommand command = new NotificationCommand(
            "user@marketplace.local",
            "user",
            NotificationEventType.WAITLIST_TICKETS_AVAILABLE,
            Map.of("eventName", "PSG vs OM")
        );

        assertThatThrownBy(() -> strategy.build(command))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.NOTIFICATION_TEMPLATE_PAYLOAD_INVALID);
    }
}
