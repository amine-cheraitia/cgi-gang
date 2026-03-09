package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.notification.application.event.ListingCertifiedApplicationEvent;
import com.marketplace.notification.application.event.WaitlistTicketsAvailableApplicationEvent;
import com.marketplace.shared.application.event.ApplicationEventDispatcher;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.waitlist.domain.model.WaitlistSubscription;
import com.marketplace.waitlist.domain.repository.WaitlistSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CertifyListingUseCase {
    private final ListingRepository listingRepository;
    private final WaitlistSubscriptionRepository waitlistRepository;
    private final ApplicationEventDispatcher eventDispatcher;

    public CertifyListingUseCase(ListingRepository listingRepository,
                                 WaitlistSubscriptionRepository waitlistRepository,
                                 ApplicationEventDispatcher eventDispatcher) {
        this.listingRepository = listingRepository;
        this.waitlistRepository = waitlistRepository;
        this.eventDispatcher = eventDispatcher;
    }

    public Listing execute(String listingId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LISTING_NOT_FOUND));
        listing.certify();
        Listing saved = listingRepository.save(listing);

        // Notifie le vendeur
        eventDispatcher.dispatch(new ListingCertifiedApplicationEvent(
            saved.getId(),
            saved.getSellerId(),
            saved.getExternalEventId().value()
        ));

        // FIFO : notifie uniquement le PREMIER inscrit en attente (WAITING)
        String startingPrice = saved.getPrice().amount().toPlainString()
            + " " + saved.getPrice().currency().getCurrencyCode();

        Optional<WaitlistSubscription> nextInLine =
            waitlistRepository.findFirstWaitingByEventId(saved.getExternalEventId().value());

        nextInLine.ifPresent(subscription -> {
            subscription.markNotified();
            waitlistRepository.save(subscription);
            eventDispatcher.dispatch(new WaitlistTicketsAvailableApplicationEvent(
                saved.getExternalEventId().value(),
                subscription.getUserId(),
                startingPrice
            ));
        });

        return saved;
    }
}
