package com.marketplace.user.infrastructure.rest.dto;

public record UserProfileResponse(
        String username,
        String email,
        String role
) {}

