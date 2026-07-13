package com.quan.url_shorten_service.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ApiDtoValidationTests {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void acceptsValidShortUrlRequest() {
        var request = new CreateShortUrlRequest(
                "https://example.com/a/long/path",
                Instant.now().plusSeconds(3600)
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsInvalidUrlAndPastExpiration() {
        var request = new CreateShortUrlRequest("not-a-url", Instant.now().minusSeconds(1));

        assertThat(validator.validate(request)).hasSize(2);
    }

    @Test
    void rejectsInvalidRegistrationData() {
        var request = new RegisterRequest("invalid", "short");

        assertThat(validator.validate(request)).hasSize(2);
    }

    @Test
    void authResponseUsesBearerTokenType() {
        var response = new AuthResponse("token", null, "person@example.com");

        assertThat(response.tokenType()).isEqualTo("Bearer");
    }
}
