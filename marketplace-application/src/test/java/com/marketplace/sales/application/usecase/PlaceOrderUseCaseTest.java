package com.marketplace.sales.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.model.ListingStatus;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.sales.domain.model.Order;
import com.marketplace.sales.domain.repository.OrderRepository;
import com.marketplace.shared.application.event.ApplicationEventDispatcher;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceOrderUseCaseTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ApplicationEventDispatcher eventDispatcher;
    @InjectMocks
    private PlaceOrderUseCase useCase;

    @Test
    void shouldPlaceOrderAndDispatchEvent() {
        Listing listing = Listing.rehydrate(
            "listing-1",
            new ExternalEventId("evt-1"),
            "seller-1",
            Money.euros(100),
            ListingStatus.CERTIFIED
        );
        when(listingRepository.findById("listing-1")).thenReturn(Optional.of(listing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = useCase.execute("listing-1", "buyer-1");

        assertThat(result.getListingId()).isEqualTo("listing-1");
        assertThat(result.getBuyerId()).isEqualTo("buyer-1");
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventDispatcher).dispatch((com.marketplace.shared.application.event.ApplicationEvent) eventCaptor.capture());
        assertThat(eventCaptor.getValue().getClass().getSimpleName()).isEqualTo("OrderPlacedApplicationEvent");
    }

    @Test
    void shouldRejectWhenListingMissing() {
        when(listingRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("missing", "buyer-1"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.LISTING_NOT_FOUND);
    }

    @Test
    void shouldRejectWhenListingNotCertified() {
        Listing listing = Listing.rehydrate(
            "listing-1",
            new ExternalEventId("evt-1"),
            "seller-1",
            Money.euros(100),
            ListingStatus.PENDING_CERTIFICATION
        );
        when(listingRepository.findById("listing-1")).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> useCase.execute("listing-1", "buyer-1"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.LISTING_NOT_CERTIFIED);
    }
}
