package com.marketplace.notification.application.usecase;

import com.marketplace.notification.application.model.EmailMessage;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.template.NotificationTemplateFactory;
import com.marketplace.notification.domain.port.EmailSender;
import org.springframework.stereotype.Service;

@Service
public class SendNotificationUseCase {
    private final NotificationTemplateFactory templateFactory;
    private final EmailSender emailSender;

    public SendNotificationUseCase(NotificationTemplateFactory templateFactory, EmailSender emailSender) {
        this.templateFactory = templateFactory;
        this.emailSender = emailSender;
    }

    public void execute(NotificationCommand command) {
        EmailMessage message = templateFactory.resolve(command.eventType()).build(command);
        emailSender.sendEmail(command.to(), message.subject(), message.body());
    }
}
