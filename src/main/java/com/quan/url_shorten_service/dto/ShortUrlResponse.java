package com.quan.url_shorten_service.dto;

import java.time.Instant;
import java.util.UUID;

public record ShortUrlResponse(
        UUID id,
        String code,
        String url,
        Instant expiresAt,
        long hits,
        Instant lastAccessedAt,
        Instant createdAt
) {
}
