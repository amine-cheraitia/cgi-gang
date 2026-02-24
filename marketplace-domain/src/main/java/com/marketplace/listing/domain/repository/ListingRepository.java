package com.marketplace.listing.domain.repository;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.valueobject.ExternalEventId;

import java.util.List;
import java.util.Optional;

public interface ListingRepository {
    Listing save(Listing listing);

    Optional<Listing> findById(String listingId);

    int countCertifiedByEvent(ExternalEventId eventId);

    Optional<Listing> findCheapestCertifiedByEvent(ExternalEventId eventId);

    Optional<Listing> findMostExpensiveCertifiedByEvent(ExternalEventId eventId);

    List<Listing> findAllCertifiedByEvent(ExternalEventId eventId);

    List<Listing> findAllCertified();

    List<Listing> findAllPendingCertification();
}
