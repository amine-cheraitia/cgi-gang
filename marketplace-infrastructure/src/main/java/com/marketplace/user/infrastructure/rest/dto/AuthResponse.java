package com.marketplace.user.infrastructure.rest.dto;

public record AuthResponse(
        String id,
        String username,
        String email,
        String role
) {}
