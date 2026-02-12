package com.marketplace.sales.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.model.ListingStatus;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.notification.application.event.OrderPlacedApplicationEvent;
import com.marketplace.sales.domain.model.Order;
import com.marketplace.sales.domain.repository.OrderRepository;
import com.marketplace.shared.application.event.ApplicationEventDispatcher;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class PlaceOrderUseCase {
    private final ListingRepository listingRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventDispatcher eventDispatcher;

    public PlaceOrderUseCase(ListingRepository listingRepository,
                             OrderRepository orderRepository,
                             ApplicationEventDispatcher eventDispatcher) {
        this.listingRepository = listingRepository;
        this.orderRepository = orderRepository;
        this.eventDispatcher = eventDispatcher;
    }

    public Order execute(String listingId, String buyerId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LISTING_NOT_FOUND));
        if (listing.getStatus() != ListingStatus.CERTIFIED) {
            throw new BusinessException(ErrorCode.LISTING_NOT_CERTIFIED);
        }

        Order order = Order.place(
            listing.getId(),
            buyerId,
            listing.getSellerId(),
            listing.getPrice()
        );
        Order saved = orderRepository.save(order);
        eventDispatcher.dispatch(new OrderPlacedApplicationEvent(
            saved.getId(),
            buyerId,
            saved.getBuyerTotal().amount().toPlainString() + " " + saved.getBuyerTotal().currency().getCurrencyCode()
        ));
        return saved;
    }
}
