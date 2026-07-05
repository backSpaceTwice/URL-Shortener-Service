package com.quan.url_shorten_service.Entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "original_urls")
public class OriginalUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
