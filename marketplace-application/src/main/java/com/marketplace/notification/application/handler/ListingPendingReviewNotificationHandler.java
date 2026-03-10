package com.marketplace.notification.application.handler;

import com.marketplace.notification.application.event.ListingPendingReviewApplicationEvent;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;
import com.marketplace.notification.application.usecase.SendNotificationUseCase;
import com.marketplace.shared.application.event.ApplicationEvent;
import com.marketplace.shared.application.event.ApplicationEventHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ListingPendingReviewNotificationHandler implements ApplicationEventHandler<ListingPendingReviewApplicationEvent> {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final String controllerEmail;

    public ListingPendingReviewNotificationHandler(
            SendNotificationUseCase sendNotificationUseCase,
            @Value("${notification.controller-email:controller@marketplace.local}") String controllerEmail
    ) {
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.controllerEmail = controllerEmail;
    }

    @Override
    public boolean supports(ApplicationEvent event) {
        return event instanceof ListingPendingReviewApplicationEvent;
    }

    @Override
    public void handle(ListingPendingReviewApplicationEvent event) {
        NotificationCommand command = new NotificationCommand(
                controllerEmail,
                "controller",
                NotificationEventType.CONTROLLER_LISTINGS_PENDING,
                Map.of(
                        "eventId", event.eventId(),
                        "listingId", event.listingId()
                )
        );
        sendNotificationUseCase.execute(command);
    }
}

