package com.quan.url_shorten_service.service;

import com.quan.url_shorten_service.Entity.OriginalUrl;
import com.quan.url_shorten_service.Entity.ShortUrl;
import com.quan.url_shorten_service.Entity.User;
import com.quan.url_shorten_service.Repository.OriginalUrlRepository;
import com.quan.url_shorten_service.Repository.ShortUrlRepository;
import com.quan.url_shorten_service.Repository.UserRepository;
import com.quan.url_shorten_service.dto.CreateShortUrlRequest;
import com.quan.url_shorten_service.exception.ExpiredShortUrlException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ShortUrlServiceTests {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Autowired
    private OriginalUrlRepository originalUrlRepository;

    @Autowired
    private UserRepository userRepository;

    private ShortUrlService service;

    @BeforeEach
    void setUp() {
        service = new ShortUrlService(shortUrlRepository, originalUrlRepository);
    }

    @Test
    void createsShortUrlForExistingOriginalUrl() {
        OriginalUrl originalUrl = originalUrlRepository.save(originalUrl("https://example.com/long"));
        User owner = userRepository.save(user("person@example.com"));

        var response = service.create(new CreateShortUrlRequest(originalUrl.getUrl(), null), owner);

        assertThat(response.id()).isNotNull();
        assertThat(response.code()).hasSize(7);
        assertThat(response.url()).isEqualTo(originalUrl.getUrl());
        assertThat(shortUrlRepository.findByCode(response.code())).isPresent();
    }

    @Test
    void resolvesUrlAndUpdatesAnalytics() {
        OriginalUrl originalUrl = originalUrlRepository.save(originalUrl("https://example.com/long"));
        ShortUrl shortUrl = shortUrlRepository.save(shortUrl("abc1234", originalUrl, null));
        shortUrl.setHits(4);

        assertThat(service.resolve("abc1234")).isEqualTo(originalUrl.getUrl());

        ShortUrl updated = shortUrlRepository.findByCode("abc1234").orElseThrow();
        assertThat(updated.getHits()).isEqualTo(5);
        assertThat(updated.getLastAccessedAt()).isNotNull();
    }

    @Test
    void refusesToResolveExpiredUrl() {
        OriginalUrl originalUrl = originalUrlRepository.save(originalUrl("https://example.com/expired"));
        ShortUrl shortUrl = shortUrl("expired", originalUrl, null);
        shortUrl.setExpiresAt(Instant.now().minusSeconds(1));
        shortUrlRepository.save(shortUrl);

        assertThatThrownBy(() -> service.resolve("expired"))
                .isInstanceOf(ExpiredShortUrlException.class);
    }

    private OriginalUrl originalUrl(String value) {
        OriginalUrl originalUrl = new OriginalUrl();
        originalUrl.setUrl(value);
        return originalUrl;
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("encoded-password");
        return user;
    }

    private ShortUrl shortUrl(String code, OriginalUrl originalUrl, User owner) {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setCode(code);
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setUser(owner);
        return shortUrl;
    }
}
