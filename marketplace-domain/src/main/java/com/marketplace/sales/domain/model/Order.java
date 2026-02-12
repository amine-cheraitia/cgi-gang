package com.marketplace.sales.domain.model;

import com.marketplace.sales.domain.valueobject.PricingBreakdown;
import com.marketplace.shared.domain.valueobject.Money;

import java.util.UUID;

public class Order {
    private final String id;
    private final String listingId;
    private final String buyerId;
    private final String sellerId;
    private final PricingBreakdown pricing;
    private OrderStatus status;

    private Order(String id, String listingId, String buyerId, String sellerId, PricingBreakdown pricing, OrderStatus status) {
        this.id = id;
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.pricing = pricing;
        this.status = status;
    }

    public static Order place(String listingId, String buyerId, String sellerId, Money ticketPrice) {
        if (listingId == null || listingId.isBlank()) {
            throw new IllegalArgumentException("Listing id is required");
        }
        if (buyerId == null || buyerId.isBlank()) {
            throw new IllegalArgumentException("Buyer id is required");
        }
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Seller id is required");
        }
        PricingBreakdown pricing = PricingBreakdown.calculate(ticketPrice);
        return new Order(UUID.randomUUID().toString(), listingId, buyerId, sellerId, pricing, OrderStatus.PENDING_PAYMENT);
    }

    public static Order rehydrate(String id, String listingId, String buyerId, String sellerId, PricingBreakdown pricing, OrderStatus status) {
        return new Order(id, listingId, buyerId, sellerId, pricing, status);
    }

    public void confirmPayment() {
        if (status == OrderStatus.PAID) {
            throw new IllegalStateException("Order already paid");
        }
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Order cannot be paid in current state");
        }
        this.status = OrderStatus.PAID;
    }

    public Money getBuyerTotal() { return pricing.buyerTotal(); }
    public Money getSellerPayout() { return pricing.sellerPayout(); }
    public Money getPlatformRevenue() { return pricing.platformRevenue(); }

    public String getId() { return id; }
    public String getListingId() { return listingId; }
    public String getBuyerId() { return buyerId; }
    public String getSellerId() { return sellerId; }
    public PricingBreakdown getPricing() { return pricing; }
    public OrderStatus getStatus() { return status; }
}
