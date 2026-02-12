package com.marketplace.listing.domain.service;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private ListingRepository listingRepository;

    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService(listingRepository);
    }

    @Test
    void shouldReturnTrueWhenListingsAvailable() {
        ExternalEventId eventId = new ExternalEventId("evt_1");
        when(listingRepository.countCertifiedByEvent(eventId)).thenReturn(3);

        assertThat(availabilityService.checkAvailability(eventId)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoListings() {
        ExternalEventId eventId = new ExternalEventId("evt_1");
        when(listingRepository.countCertifiedByEvent(eventId)).thenReturn(0);

        assertThat(availabilityService.checkAvailability(eventId)).isFalse();
    }

    @Test
    void shouldReturnCheapestListing() {
        ExternalEventId eventId = new ExternalEventId("evt_1");
        Listing listing = Listing.create(eventId, "seller-1", Money.euros(45));
        listing.certify();
        when(listingRepository.findCheapestCertifiedByEvent(eventId)).thenReturn(Optional.of(listing));

        assertThat(availabilityService.findCheapestListing(eventId)).contains(listing);
    }

    @Test
    void shouldCountAvailableListingsAndDetectSoldOut() {
        ExternalEventId eventId = new ExternalEventId("evt_1");
        when(listingRepository.countCertifiedByEvent(eventId)).thenReturn(2).thenReturn(0);

        assertThat(availabilityService.countAvailableListings(eventId)).isEqualTo(2);
        assertThat(availabilityService.isEventSoldOut(eventId)).isTrue();
    }

    @Test
    void shouldReturnPriceRangeWhenBothBoundariesExist() {
        ExternalEventId eventId = new ExternalEventId("evt_1");
        Listing min = Listing.create(eventId, "seller-1", Money.euros(40));
        Listing max = Listing.create(eventId, "seller-2", Money.euros(100));
        min.certify();
        max.certify();
        when(listingRepository.findCheapestCertifiedByEvent(eventId)).thenReturn(Optional.of(min));
        when(listingRepository.findMostExpensiveCertifiedByEvent(eventId)).thenReturn(Optional.of(max));

        assertThat(availabilityService.getPriceRange(eventId)).isPresent();
        assertThat(availabilityService.getPriceRange(eventId).get().min()).isEqualTo(Money.euros(40));
        assertThat(availabilityService.getPriceRange(eventId).get().max()).isEqualTo(Money.euros(100));
    }

    @Test
    void shouldReturnEmptyPriceRangeWhenBoundaryMissing() {
        ExternalEventId eventId = new ExternalEventId("evt_1");
        when(listingRepository.findCheapestCertifiedByEvent(eventId)).thenReturn(Optional.empty());
        when(listingRepository.findMostExpensiveCertifiedByEvent(eventId))
            .thenReturn(Optional.of(Listing.create(eventId, "seller-1", Money.euros(80))));

        assertThat(availabilityService.getPriceRange(eventId)).isEmpty();
    }
}
