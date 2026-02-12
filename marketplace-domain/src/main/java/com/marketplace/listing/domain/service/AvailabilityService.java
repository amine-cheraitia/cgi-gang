package com.marketplace.listing.domain.service;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.listing.domain.valueobject.PriceRange;

import java.util.Optional;

public class AvailabilityService {
    private final ListingRepository listingRepository;

    public AvailabilityService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public boolean checkAvailability(ExternalEventId eventId) {
        return listingRepository.countCertifiedByEvent(eventId) > 0;
    }

    public int countAvailableListings(ExternalEventId eventId) {
        return listingRepository.countCertifiedByEvent(eventId);
    }

    public Optional<Listing> findCheapestListing(ExternalEventId eventId) {
        return listingRepository.findCheapestCertifiedByEvent(eventId);
    }

    public boolean isEventSoldOut(ExternalEventId eventId) {
        return !checkAvailability(eventId);
    }

    public Optional<PriceRange> getPriceRange(ExternalEventId eventId) {
        Optional<Listing> cheapest = listingRepository.findCheapestCertifiedByEvent(eventId);
        Optional<Listing> expensive = listingRepository.findMostExpensiveCertifiedByEvent(eventId);

        if (cheapest.isEmpty() || expensive.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new PriceRange(
            cheapest.get().getPrice(),
            expensive.get().getPrice()
        ));
    }
}
