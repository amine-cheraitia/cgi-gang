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
}
