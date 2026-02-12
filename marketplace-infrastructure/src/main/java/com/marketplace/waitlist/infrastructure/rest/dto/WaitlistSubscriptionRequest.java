package com.marketplace.waitlist.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record WaitlistSubscriptionRequest(
    @NotBlank String eventId,
    @NotBlank String userId
) {
}
