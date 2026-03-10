package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.notification.application.event.ListingPendingReviewApplicationEvent;
import com.marketplace.shared.application.event.ApplicationEventDispatcher;
import com.marketplace.shared.domain.valueobject.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;

@Service
public class CreateListingUseCase {
    private final ListingRepository listingRepository;
    private final ApplicationEventDispatcher eventDispatcher;

    public CreateListingUseCase(ListingRepository listingRepository,
                                ApplicationEventDispatcher eventDispatcher) {
        this.listingRepository = listingRepository;
        this.eventDispatcher = eventDispatcher;
    }

    public Listing execute(String eventId, String sellerId, BigDecimal price, String currencyCode) {
        Listing listing = Listing.create(
            new ExternalEventId(eventId),
            sellerId,
            Money.of(price, Currency.getInstance(currencyCode))
        );
        Listing saved = listingRepository.save(listing);

        // Notifie les contrôleurs qu'une nouvelle annonce est en attente de certification
        eventDispatcher.dispatch(new ListingPendingReviewApplicationEvent(
                saved.getId(),
                saved.getExternalEventId().value()
        ));

        return saved;
    }
}
