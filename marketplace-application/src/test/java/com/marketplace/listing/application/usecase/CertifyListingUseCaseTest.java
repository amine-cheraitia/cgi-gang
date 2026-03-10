package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.model.ListingStatus;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.shared.application.event.ApplicationEventDispatcher;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.shared.domain.valueobject.Money;
import com.marketplace.waitlist.domain.repository.WaitlistSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertifyListingUseCaseTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private WaitlistSubscriptionRepository waitlistRepository;
    @Mock
    private ApplicationEventDispatcher dispatcher;

    @InjectMocks
    private CertifyListingUseCase useCase;

    @Test
    void shouldRejectCertificationWhenNoTicketAttachment() {
        Listing listing = Listing.rehydrate(
                "listing-1",
                new ExternalEventId("evt-1"),
                "seller-1",
                Money.euros(50),
                ListingStatus.PENDING_CERTIFICATION,
                false
        );
        when(listingRepository.findById("listing-1")).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> useCase.execute("listing-1"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.LISTING_MISSING_TICKET_ATTACHMENT);
    }
}

