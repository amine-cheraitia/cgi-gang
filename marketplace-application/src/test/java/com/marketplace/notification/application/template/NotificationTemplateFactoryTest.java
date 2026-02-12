package com.marketplace.notification.application.template;

import com.marketplace.notification.application.model.NotificationEventType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTemplateFactoryTest {

    @Test
    void shouldResolveStrategyByEventType() {
        NotificationTemplateFactory factory = new NotificationTemplateFactory(List.of(
            new ListingCertifiedTemplateStrategy(),
            new OrderPlacedTemplateStrategy(),
            new OrderPaidTemplateStrategy(),
            new WaitlistAvailableTemplateStrategy()
        ));

        EmailTemplateStrategy strategy = factory.resolve(NotificationEventType.ORDER_PLACED);
        assertThat(strategy).isInstanceOf(OrderPlacedTemplateStrategy.class);
    }
}
