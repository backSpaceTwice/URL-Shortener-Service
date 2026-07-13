package com.quan.url_shorten_service.dto;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {
    public ApiErrorResponse(int status, String error, String message) {
        this(Instant.now(), status, error, message, Map.of());
    }
}
