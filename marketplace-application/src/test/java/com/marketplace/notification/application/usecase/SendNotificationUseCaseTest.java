package com.marketplace.notification.application.usecase;

import com.marketplace.notification.application.model.EmailMessage;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;
import com.marketplace.notification.application.template.EmailTemplateStrategy;
import com.marketplace.notification.application.template.NotificationTemplateFactory;
import com.marketplace.notification.domain.port.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationUseCaseTest {

    @Mock
    private NotificationTemplateFactory templateFactory;
    @Mock
    private EmailSender emailSender;
    @Mock
    private EmailTemplateStrategy strategy;
    @InjectMocks
    private SendNotificationUseCase useCase;

    @Test
    void shouldBuildTemplateAndSendEmail() {
        NotificationCommand command = new NotificationCommand(
            "buyer@marketplace.local",
            "buyer",
            NotificationEventType.ORDER_PLACED,
            Map.of("orderId", "ord-1", "buyerTotal", "100 EUR")
        );
        when(templateFactory.resolve(NotificationEventType.ORDER_PLACED)).thenReturn(strategy);
        when(strategy.build(command)).thenReturn(new EmailMessage("Sujet", "Texte", "<html>Html</html>"));

        useCase.execute(command);

        verify(emailSender).sendEmail("buyer@marketplace.local", "Sujet", "Texte", "<html>Html</html>");
    }
}
