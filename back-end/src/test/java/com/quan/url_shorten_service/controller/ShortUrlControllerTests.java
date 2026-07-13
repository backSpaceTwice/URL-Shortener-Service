package com.quan.url_shorten_service.controller;

import com.quan.url_shorten_service.Entity.User;
import com.quan.url_shorten_service.Repository.UserRepository;
import com.quan.url_shorten_service.dto.ShortUrlResponse;
import com.quan.url_shorten_service.service.ShortUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShortUrlControllerTests {

    @Mock
    private ShortUrlService shortUrlService;

    @Mock
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ShortUrlController(shortUrlService, userRepository),
                        new RedirectController(shortUrlService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsShortUrl() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(shortUrlService.create(any(), any())).thenReturn(new ShortUrlResponse(
                UUID.randomUUID(), "abc1234", "https://example.com", null, 0, null, Instant.now()
        ));

        mockMvc.perform(post("/api/urls")
                        .requestAttr(ShortUrlController.AUTHENTICATED_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/r/abc1234"))
                .andExpect(jsonPath("$.code").value("abc1234"));
    }

    @Test
    void validatesCreateRequest() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .requestAttr(ShortUrlController.AUTHENTICATED_USER_ID, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.url").exists());
    }

    @Test
    void redirectsToOriginalUrl() throws Exception {
        when(shortUrlService.resolve("abc1234")).thenReturn("https://example.com/destination");

        mockMvc.perform(get("/r/abc1234"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/destination"));
    }
}
