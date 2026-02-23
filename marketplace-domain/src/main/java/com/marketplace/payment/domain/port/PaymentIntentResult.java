package com.marketplace.payment.domain.port;

public record PaymentIntentResult(String paymentIntentId, String clientSecret) {}
