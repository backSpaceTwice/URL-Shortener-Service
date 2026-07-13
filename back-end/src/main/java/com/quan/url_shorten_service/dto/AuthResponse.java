package com.quan.url_shorten_service.dto;

import java.util.UUID;

public record AuthResponse(
        String token,
        String tokenType,
        UUID userId,
        String email
) {
    public AuthResponse(String token, UUID userId, String email) {
        this(token, "Bearer", userId, email);
    }
}
