package com.marketplace.sales.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record PlaceOrderRequest(
    @NotBlank String listingId,
    @NotBlank String buyerId
) {
}
