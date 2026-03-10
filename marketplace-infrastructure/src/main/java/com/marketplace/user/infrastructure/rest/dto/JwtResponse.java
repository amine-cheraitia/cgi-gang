package com.marketplace.user.infrastructure.rest.dto;

public record JwtResponse(
        String token,
        String tokenType
) {
    public static JwtResponse bearer(String token) {
        return new JwtResponse(token, "Bearer");
    }
}

