package com.marketplace.notification.application.handler;

import com.marketplace.notification.application.event.OrderPlacedApplicationEvent;
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
public class OrderPlacedNotificationHandler implements ApplicationEventHandler<OrderPlacedApplicationEvent> {
    private final UserContactProvider userContactProvider;
    private final SendNotificationUseCase sendNotificationUseCase;

    public OrderPlacedNotificationHandler(UserContactProvider userContactProvider,
                                          SendNotificationUseCase sendNotificationUseCase) {
        this.userContactProvider = userContactProvider;
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    @Override
    public boolean supports(ApplicationEvent event) {
        return event instanceof OrderPlacedApplicationEvent;
    }

    @Override
    public void handle(OrderPlacedApplicationEvent event) {
        UserContactProvider.UserContact buyer;
        try {
            buyer = userContactProvider.getByUserId(event.buyerId());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, ex.getMessage());
        }
        sendNotificationUseCase.execute(new NotificationCommand(
            buyer.email(),
            buyer.username(),
            NotificationEventType.ORDER_PLACED,
            Map.of(
                "orderId", event.orderId(),
                "buyerTotal", event.buyerTotal()
            )
        ));
    }
}
