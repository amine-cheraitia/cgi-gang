package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.model.ListingStatus;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.shared.application.event.ApplicationEvent;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingCoreUseCasesTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private ApplicationEventDispatcher dispatcher;

    @InjectMocks
    private CreateListingUseCase createListingUseCase;
    @InjectMocks
    private ListPublicListingsUseCase listPublicListingsUseCase;
    @InjectMocks
    private CertifyListingUseCase certifyListingUseCase;

    @Test
    void createAndListShouldDelegateToRepository() {
        Listing created = Listing.create(new ExternalEventId("evt-1"), "seller-1", Money.euros(50));
        when(listingRepository.save(any(Listing.class))).thenReturn(created);
        when(listingRepository.findAllCertified()).thenReturn(List.of(created));

        Listing result = createListingUseCase.execute("evt-1", "seller-1", BigDecimal.valueOf(50), "EUR");
        List<Listing> listed = listPublicListingsUseCase.execute();

        assertThat(result.getSellerId()).isEqualTo("seller-1");
        assertThat(listed).hasSize(1);
    }

    @Test
    void certifyShouldDispatchTwoEvents() {
        Listing listing = Listing.rehydrate(
            "listing-1",
            new ExternalEventId("evt-1"),
            "seller-1",
            Money.euros(65),
            ListingStatus.PENDING_CERTIFICATION
        );
        when(listingRepository.findById("listing-1")).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing result = certifyListingUseCase.execute("listing-1");

        assertThat(result.getStatus()).isEqualTo(ListingStatus.CERTIFIED);
        ArgumentCaptor<ApplicationEvent> events = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(dispatcher, times(2)).dispatch(events.capture());
        assertThat(events.getAllValues()).extracting(e -> e.getClass().getSimpleName())
            .containsExactly("ListingCertifiedApplicationEvent", "WaitlistTicketsAvailableApplicationEvent");
    }

    @Test
    void certifyShouldFailWhenListingMissing() {
        when(listingRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certifyListingUseCase.execute("missing"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.LISTING_NOT_FOUND);
    }
}
