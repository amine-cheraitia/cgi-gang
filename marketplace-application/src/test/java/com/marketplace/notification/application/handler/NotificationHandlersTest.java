package com.marketplace.notification.application.handler;

import com.marketplace.catalog.domain.model.ExternalEvent;
import com.marketplace.catalog.domain.port.CatalogProvider;
import com.marketplace.notification.application.event.ListingCertifiedApplicationEvent;
import com.marketplace.notification.application.event.OrderPaidApplicationEvent;
import com.marketplace.notification.application.event.OrderPlacedApplicationEvent;
import com.marketplace.notification.application.event.WaitlistTicketsAvailableApplicationEvent;
import com.marketplace.notification.application.model.NotificationCommand;
import com.marketplace.notification.application.port.UserContactProvider;
import com.marketplace.notification.application.usecase.SendNotificationUseCase;
import com.marketplace.shared.application.event.ApplicationEvent;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationHandlersTest {

    @Mock
    private UserContactProvider userContactProvider;
    @Mock
    private SendNotificationUseCase sendNotificationUseCase;
    @Mock
    private CatalogProvider catalogProvider;

    @InjectMocks
    private ListingCertifiedNotificationHandler listingCertifiedHandler;
    @InjectMocks
    private ListingPendingReviewNotificationHandler listingPendingHandler;
    @InjectMocks
    private OrderPlacedNotificationHandler orderPlacedHandler;
    @InjectMocks
    private OrderPaidNotificationHandler orderPaidHandler;
    @InjectMocks
    private WaitlistTicketsAvailableNotificationHandler waitlistHandler;

    @Test
    void listingCertifiedHandlerShouldSupportAndDispatchNotification() {
        when(userContactProvider.getByUserId("seller-1"))
            .thenReturn(new UserContactProvider.UserContact("seller-1", "seller", "seller@ticketio.app"));
        ListingCertifiedApplicationEvent event = new ListingCertifiedApplicationEvent("listing-1", "seller-1", "evt-1");

        assertThat(listingCertifiedHandler.supports(event)).isTrue();
        assertThat(listingCertifiedHandler.supports(new DummyEvent())).isFalse();

        listingCertifiedHandler.handle(event);

        ArgumentCaptor<NotificationCommand> commandCaptor = ArgumentCaptor.forClass(NotificationCommand.class);
        verify(sendNotificationUseCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().eventType().name()).isEqualTo("LISTING_CERTIFIED");
    }

    @Test
    void listingPendingHandlerShouldDispatchNotificationToController() {
        ListingPendingReviewApplicationEvent event = new ListingPendingReviewApplicationEvent("listing-1", "evt-1");

        assertThat(listingPendingHandler.supports(event)).isTrue();

        listingPendingHandler.handle(event);

        verify(sendNotificationUseCase).execute(org.mockito.ArgumentMatchers.argThat(cmd ->
            cmd.eventType() == NotificationEventType.CONTROLLER_LISTINGS_PENDING));
    }

    @Test
    void orderPlacedAndPaidHandlersShouldDispatchNotifications() {
        when(userContactProvider.getByUserId("buyer-1"))
            .thenReturn(new UserContactProvider.UserContact("buyer-1", "buyer", "buyer@ticketio.app"));
        when(userContactProvider.getByUserId("seller-1"))
            .thenReturn(new UserContactProvider.UserContact("seller-1", "seller", "seller@ticketio.app"));

        orderPlacedHandler.handle(new OrderPlacedApplicationEvent("ord-1", "buyer-1", "100 EUR"));
        orderPaidHandler.handle(new OrderPaidApplicationEvent("ord-1", "buyer-1", "seller-1", "90 EUR", "10 EUR"));

        verify(sendNotificationUseCase).execute(org.mockito.ArgumentMatchers.argThat(cmd ->
            cmd.eventType().name().equals("ORDER_PLACED")));
        verify(sendNotificationUseCase).execute(org.mockito.ArgumentMatchers.argThat(cmd ->
            cmd.eventType().name().equals("ORDER_PAID")));
    }

    @Test
    void handlersShouldMapUnknownUserToUsr001() {
        when(userContactProvider.getByUserId("unknown")).thenThrow(new IllegalArgumentException("not found"));

        assertThatThrownBy(() -> listingCertifiedHandler.handle(
            new ListingCertifiedApplicationEvent("listing-1", "unknown", "evt-1")))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void waitlistHandlerShouldNotifyTargetUserWithEventName() {
        // FIFO : l'événement contient déjà le targetUserId, le handler notifie uniquement cet utilisateur
        when(userContactProvider.getByUserId("buyer-1"))
            .thenReturn(new UserContactProvider.UserContact("buyer-1", "buyer", "buyer@ticketio.app"));
        when(catalogProvider.getEventById("evt-1"))
            .thenReturn(Optional.of(new ExternalEvent("evt-1", "PSG vs OM", Instant.now(), "Parc", "Paris")));

        waitlistHandler.handle(new WaitlistTicketsAvailableApplicationEvent("evt-1", "buyer-1", "80 EUR"));

        verify(sendNotificationUseCase).execute(org.mockito.ArgumentMatchers.argThat(cmd ->
            cmd.data().get("eventName").equals("PSG vs OM")));
    }

    @Test
    void waitlistHandlerShouldFallbackToEventIdWhenCatalogDown() {
        when(userContactProvider.getByUserId("buyer-1"))
            .thenReturn(new UserContactProvider.UserContact("buyer-1", "buyer", "buyer@ticketio.app"));
        when(catalogProvider.getEventById("evt-1")).thenThrow(new RuntimeException("catalog down"));

        waitlistHandler.handle(new WaitlistTicketsAvailableApplicationEvent("evt-1", "buyer-1", "80 EUR"));

        verify(sendNotificationUseCase).execute(org.mockito.ArgumentMatchers.argThat(cmd ->
            cmd.data().get("eventName").equals("evt-1")));
    }

    private record DummyEvent() implements ApplicationEvent {
    }
}
