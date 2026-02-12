package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.notification.application.event.ListingCertifiedApplicationEvent;
import com.marketplace.notification.application.event.WaitlistTicketsAvailableApplicationEvent;
import com.marketplace.shared.application.event.ApplicationEventDispatcher;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class CertifyListingUseCase {
    private final ListingRepository listingRepository;
    private final ApplicationEventDispatcher eventDispatcher;

    public CertifyListingUseCase(ListingRepository listingRepository,
                                 ApplicationEventDispatcher eventDispatcher) {
        this.listingRepository = listingRepository;
        this.eventDispatcher = eventDispatcher;
    }

    public Listing execute(String listingId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LISTING_NOT_FOUND));
        listing.certify();
        Listing saved = listingRepository.save(listing);
        eventDispatcher.dispatch(new ListingCertifiedApplicationEvent(
            saved.getId(),
            saved.getSellerId(),
            saved.getExternalEventId().value()
        ));
        eventDispatcher.dispatch(new WaitlistTicketsAvailableApplicationEvent(
            saved.getExternalEventId().value(),
            saved.getPrice().amount().toPlainString() + " " + saved.getPrice().currency().getCurrencyCode()
        ));
        return saved;
    }
}
