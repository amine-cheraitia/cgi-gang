package com.marketplace.sales.infrastructure.rest.dto;

import com.marketplace.sales.domain.model.Order;

import java.math.BigDecimal;

public record OrderResponse(
    String orderId,
    String listingId,
    String buyerId,
    String sellerId,
    String status,
    String paymentIntentId,
    String clientSecret,
    Pricing pricing
) {
    public record Pricing(
        BigDecimal ticketPrice,
        BigDecimal sellerFee,
        BigDecimal serviceFee,
        BigDecimal transactionFee,
        BigDecimal buyerTotal,
        BigDecimal sellerPayout,
        BigDecimal platformRevenue,
        String currency
    ) {
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getListingId(),
            order.getBuyerId(),
            order.getSellerId(),
            order.getStatus().name(),
            order.getStripePaymentIntentId(),
            order.getStripeClientSecret(),
            new Pricing(
                order.getPricing().ticketPrice().amount(),
                order.getPricing().sellerFee().amount(),
                order.getPricing().serviceFee().amount(),
                order.getPricing().transactionFee().amount(),
                order.getPricing().buyerTotal().amount(),
                order.getPricing().sellerPayout().amount(),
                order.getPricing().platformRevenue().amount(),
                order.getPricing().ticketPrice().currency().getCurrencyCode()
            )
        );
    }
}
