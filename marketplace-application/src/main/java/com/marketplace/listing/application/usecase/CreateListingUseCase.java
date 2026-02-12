package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.shared.domain.valueobject.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;

@Service
public class CreateListingUseCase {
    private final ListingRepository listingRepository;

    public CreateListingUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public Listing execute(String eventId, String sellerId, BigDecimal price, String currencyCode) {
        Listing listing = Listing.create(
            new ExternalEventId(eventId),
            sellerId,
            Money.of(price, Currency.getInstance(currencyCode))
        );
        return listingRepository.save(listing);
    }
}
