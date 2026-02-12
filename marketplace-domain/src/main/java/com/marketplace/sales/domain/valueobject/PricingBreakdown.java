package com.marketplace.sales.domain.valueobject;

import com.marketplace.shared.domain.valueobject.Money;

import java.math.BigDecimal;

public record PricingBreakdown(
    Money ticketPrice,
    Money sellerFee,
    Money serviceFee,
    Money transactionFee,
    Money buyerTotal,
    Money sellerPayout
) {
    private static final BigDecimal SELLER_FEE_RATE = new BigDecimal("0.05");
    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.10");
    private static final BigDecimal TRANSACTION_FEE_RATE = new BigDecimal("0.025");

    public PricingBreakdown {
        if (ticketPrice == null || sellerFee == null || serviceFee == null
            || transactionFee == null || buyerTotal == null || sellerPayout == null) {
            throw new IllegalArgumentException("Pricing fields cannot be null");
        }
    }

    public static PricingBreakdown calculate(Money ticketPrice) {
        if (ticketPrice == null) {
            throw new IllegalArgumentException("Ticket price cannot be null");
        }
        if (ticketPrice.isNegative()) {
            throw new IllegalArgumentException("Ticket price cannot be negative");
        }

        Money sellerFee = ticketPrice.multiply(SELLER_FEE_RATE);
        Money serviceFee = ticketPrice.multiply(SERVICE_FEE_RATE);
        Money transactionFee = ticketPrice.multiply(TRANSACTION_FEE_RATE);
        Money buyerTotal = ticketPrice.add(serviceFee).add(transactionFee);
        Money sellerPayout = ticketPrice.subtract(sellerFee);

        return new PricingBreakdown(
            ticketPrice,
            sellerFee,
            serviceFee,
            transactionFee,
            buyerTotal,
            sellerPayout
        );
    }

    public Money platformRevenue() {
        return sellerFee.add(serviceFee).add(transactionFee);
    }
}
