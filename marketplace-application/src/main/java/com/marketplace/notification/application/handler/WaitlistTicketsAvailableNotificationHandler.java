package com.marketplace.notification.application.handler;

import com.marketplace.catalog.domain.port.CatalogProvider;
import com.marketplace.notification.application.event.WaitlistTicketsAvailableApplicationEvent;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;
import com.marketplace.notification.application.port.UserContactProvider;
import com.marketplace.notification.application.usecase.SendNotificationUseCase;
import com.marketplace.shared.application.event.ApplicationEvent;
import com.marketplace.shared.application.event.ApplicationEventHandler;
import com.marketplace.waitlist.domain.repository.WaitlistSubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WaitlistTicketsAvailableNotificationHandler
    implements ApplicationEventHandler<WaitlistTicketsAvailableApplicationEvent> {

    private final WaitlistSubscriptionRepository waitlistSubscriptionRepository;
    private final CatalogProvider catalogProvider;
    private final UserContactProvider userContactProvider;
    private final SendNotificationUseCase sendNotificationUseCase;

    public WaitlistTicketsAvailableNotificationHandler(WaitlistSubscriptionRepository waitlistSubscriptionRepository,
                                                       CatalogProvider catalogProvider,
                                                       UserContactProvider userContactProvider,
                                                       SendNotificationUseCase sendNotificationUseCase) {
        this.waitlistSubscriptionRepository = waitlistSubscriptionRepository;
        this.catalogProvider = catalogProvider;
        this.userContactProvider = userContactProvider;
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    @Override
    public boolean supports(ApplicationEvent event) {
        return event instanceof WaitlistTicketsAvailableApplicationEvent;
    }

    @Override
    public void handle(WaitlistTicketsAvailableApplicationEvent event) {
        String displayEventName = resolveEventDisplayName(event.eventId());
        waitlistSubscriptionRepository.findByEventId(event.eventId()).forEach(subscription -> {
            UserContactProvider.UserContact user = userContactProvider.getByUserId(subscription.getUserId());
            sendNotificationUseCase.execute(new NotificationCommand(
                user.email(),
                user.username(),
                NotificationEventType.WAITLIST_TICKETS_AVAILABLE,
                Map.of(
                    "eventName", displayEventName,
                    "startingPrice", event.startingPrice()
                )
            ));
        });
    }

    private String resolveEventDisplayName(String eventId) {
        try {
            return catalogProvider.getEventById(eventId)
                .map(evt -> evt.name())
                .orElse(eventId);
        } catch (RuntimeException ex) {
            return eventId;
        }
    }
}
