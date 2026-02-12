package com.marketplace.payment.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentWebhookRequest(
    @NotBlank String orderId,
    @NotBlank String status,
    String providerTransactionId
) {
}
