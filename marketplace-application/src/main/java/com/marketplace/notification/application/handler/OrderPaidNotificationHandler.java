package com.marketplace.notification.application.handler;

import com.marketplace.notification.application.event.OrderPaidApplicationEvent;
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
public class OrderPaidNotificationHandler implements ApplicationEventHandler<OrderPaidApplicationEvent> {
    private final SendNotificationUseCase sendNotificationUseCase;
    private final UserContactProvider userContactProvider;

    public OrderPaidNotificationHandler(SendNotificationUseCase sendNotificationUseCase,
                                        UserContactProvider userContactProvider) {
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.userContactProvider = userContactProvider;
    }

    @Override
    public boolean supports(ApplicationEvent event) {
        return event instanceof OrderPaidApplicationEvent;
    }

    @Override
    public void handle(OrderPaidApplicationEvent event) {
        UserContactProvider.UserContact sellerContact;
        UserContactProvider.UserContact buyerContact;
        try {
            sellerContact = userContactProvider.getByUserId(event.sellerId());
            buyerContact = userContactProvider.getByUserId(event.buyerId());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, ex.getMessage());
        }

        // Email vendeur : focus sur le montant qu'il touche et la commission
        sendNotificationUseCase.execute(new NotificationCommand(
            sellerContact.email(),
            sellerContact.username(),
            NotificationEventType.ORDER_PAID,
            Map.of(
                "orderId", event.orderId(),
                "role", "SELLER",
                "buyerTotal", event.buyerTotal(),
                "sellerPayout", event.sellerPayout(),
                "platformRevenue", event.platformRevenue()
            )
        ));

        // Email acheteur : confirmation de paiement et montant payé
        sendNotificationUseCase.execute(new NotificationCommand(
            buyerContact.email(),
            buyerContact.username(),
            NotificationEventType.ORDER_PAID,
            Map.of(
                "orderId", event.orderId(),
                "role", "BUYER",
                "buyerTotal", event.buyerTotal()
            )
        ));
    }
}
