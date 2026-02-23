package com.marketplace.payment.domain.port;

public interface PaymentGateway {
    PaymentIntentResult createPaymentIntent(String orderId, long amountCents, String currency);
}
