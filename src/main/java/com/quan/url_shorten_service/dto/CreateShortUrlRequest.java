package com.quan.url_shorten_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

public record CreateShortUrlRequest(
        @NotBlank @URL String url,
        @Future Instant expiresAt
) {
}
