package com.quan.url_shorten_service.controller;

import com.quan.url_shorten_service.Entity.User;
import com.quan.url_shorten_service.Repository.UserRepository;
import com.quan.url_shorten_service.dto.CreateShortUrlRequest;
import com.quan.url_shorten_service.dto.ShortUrlResponse;
import com.quan.url_shorten_service.exception.ResourceNotFoundException;
import com.quan.url_shorten_service.service.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/urls")
public class ShortUrlController {

    public static final String AUTHENTICATED_USER_ID = "authenticatedUserId";

    private final ShortUrlService shortUrlService;
    private final UserRepository userRepository;

    public ShortUrlController(ShortUrlService shortUrlService, UserRepository userRepository) {
        this.shortUrlService = shortUrlService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ShortUrlResponse> create(
            @Valid @RequestBody CreateShortUrlRequest request,
            @RequestAttribute(value = AUTHENTICATED_USER_ID, required = false) UUID userId
    ) {
        User user = userId == null ? null : userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        ShortUrlResponse response = shortUrlService.create(request, user);
        return ResponseEntity.created(URI.create("/r/" + response.code())).body(response);
    }

    @GetMapping
    public List<ShortUrlResponse> findMine(
            @RequestAttribute(AUTHENTICATED_USER_ID) UUID userId
    ) {
        return shortUrlService.findByUserId(userId);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(
            @PathVariable String code,
            @RequestAttribute(AUTHENTICATED_USER_ID) UUID userId
    ) {
        shortUrlService.delete(code, userId);
        return ResponseEntity.noContent().build();
    }
}
