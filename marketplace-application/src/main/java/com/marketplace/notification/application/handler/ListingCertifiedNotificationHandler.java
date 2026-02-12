package com.marketplace.notification.application.handler;

import com.marketplace.notification.application.event.ListingCertifiedApplicationEvent;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.model.NotificationEventType;
import com.marketplace.notification.application.port.UserContactProvider;
import com.marketplace.notification.application.usecase.SendNotificationUseCase;
import com.marketplace.shared.application.event.ApplicationEvent;
import com.marketplace.shared.application.event.ApplicationEventHandler;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ListingCertifiedNotificationHandler implements ApplicationEventHandler<ListingCertifiedApplicationEvent> {
    private final UserContactProvider userContactProvider;
    private final SendNotificationUseCase sendNotificationUseCase;

    public ListingCertifiedNotificationHandler(UserContactProvider userContactProvider,
                                               SendNotificationUseCase sendNotificationUseCase) {
        this.userContactProvider = userContactProvider;
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    @Override
    public boolean supports(ApplicationEvent event) {
        return event instanceof ListingCertifiedApplicationEvent;
    }

    @Override
    public void handle(ListingCertifiedApplicationEvent event) {
        UserContactProvider.UserContact seller;
        try {
            seller = userContactProvider.getByUserId(event.sellerId());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, ex.getMessage());
        }
        sendNotificationUseCase.execute(new NotificationCommand(
            seller.email(),
            seller.username(),
            NotificationEventType.LISTING_CERTIFIED,
            Map.of(
                "eventName", event.eventId(),
                "listingId", event.listingId()
            )
        ));
    }
}
