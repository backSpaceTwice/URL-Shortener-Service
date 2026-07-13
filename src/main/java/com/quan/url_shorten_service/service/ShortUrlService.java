package com.quan.url_shorten_service.service;

import com.quan.url_shorten_service.Entity.OriginalUrl;
import com.quan.url_shorten_service.Entity.ShortUrl;
import com.quan.url_shorten_service.Entity.User;
import com.quan.url_shorten_service.Repository.OriginalUrlRepository;
import com.quan.url_shorten_service.Repository.ShortUrlRepository;
import com.quan.url_shorten_service.dto.CreateShortUrlRequest;
import com.quan.url_shorten_service.dto.ShortUrlResponse;
import com.quan.url_shorten_service.exception.ExpiredShortUrlException;
import com.quan.url_shorten_service.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ShortUrlService {

    private static final String CODE_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 7;
    private static final int MAX_CODE_ATTEMPTS = 10;

    private final ShortUrlRepository shortUrlRepository;
    private final OriginalUrlRepository originalUrlRepository;
    private final SecureRandom secureRandom;

    public ShortUrlService(ShortUrlRepository shortUrlRepository, OriginalUrlRepository originalUrlRepository) {
        this(shortUrlRepository, originalUrlRepository, new SecureRandom());
    }

    ShortUrlService(
            ShortUrlRepository shortUrlRepository,
            OriginalUrlRepository originalUrlRepository,
            SecureRandom secureRandom
    ) {
        this.shortUrlRepository = shortUrlRepository;
        this.originalUrlRepository = originalUrlRepository;
        this.secureRandom = secureRandom;
    }

    @Transactional
    public ShortUrlResponse create(CreateShortUrlRequest request, User owner) {
        OriginalUrl originalUrl = originalUrlRepository.findByUrl(request.url())
                .orElseGet(() -> saveOriginalUrl(request.url()));

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setCode(generateUniqueCode());
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setUser(owner);
        shortUrl.setExpiresAt(request.expiresAt());

        return toResponse(shortUrlRepository.save(shortUrl));
    }

    @Transactional
    public String resolve(String code) {
        ShortUrl shortUrl = shortUrlRepository.findByCodeWithOriginalUrl(code)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found"));

        if (shortUrl.getExpiresAt() != null && !shortUrl.getExpiresAt().isAfter(Instant.now())) {
            throw new ExpiredShortUrlException("Short URL has expired");
        }

        shortUrl.setHits(shortUrl.getHits() + 1);
        shortUrl.setLastAccessedAt(Instant.now());
        shortUrlRepository.save(shortUrl);
        return shortUrl.getOriginalUrl().getUrl();
    }

    @Transactional(readOnly = true)
    public List<ShortUrlResponse> findByUserId(UUID userId) {
        return shortUrlRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(String code, UUID userId) {
        ShortUrl shortUrl = shortUrlRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found"));

        if (shortUrl.getUser() == null || !shortUrl.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Short URL not found");
        }
        shortUrlRepository.delete(shortUrl);
    }

    private OriginalUrl saveOriginalUrl(String url) {
        OriginalUrl originalUrl = new OriginalUrl();
        originalUrl.setUrl(url);
        return originalUrlRepository.save(originalUrl);
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            StringBuilder code = new StringBuilder(CODE_LENGTH);
            for (int index = 0; index < CODE_LENGTH; index++) {
                code.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
            }
            if (!shortUrlRepository.existsByCode(code.toString())) {
                return code.toString();
            }
        }
        throw new IllegalStateException("Could not generate a unique short code");
    }

    private ShortUrlResponse toResponse(ShortUrl shortUrl) {
        return new ShortUrlResponse(
                shortUrl.getId(),
                shortUrl.getCode(),
                shortUrl.getOriginalUrl().getUrl(),
                shortUrl.getExpiresAt(),
                shortUrl.getHits(),
                shortUrl.getLastAccessedAt(),
                shortUrl.getCreatedAt()
        );
    }
}
