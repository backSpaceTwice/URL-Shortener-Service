package com.quan.url_shorten_service.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "original_urls")
@Getter
@Setter
@NoArgsConstructor
public class OriginalUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
