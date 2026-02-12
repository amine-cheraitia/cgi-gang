package com.marketplace.listing.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateListingRequest(
    @NotBlank String eventId,
    @NotBlank String sellerId,
    @NotNull @DecimalMin("0.0") BigDecimal price,
    @NotBlank String currency
) {
}
