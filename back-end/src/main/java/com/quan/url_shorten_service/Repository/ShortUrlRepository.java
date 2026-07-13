package com.quan.url_shorten_service.Repository;

import com.quan.url_shorten_service.Entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, UUID> {

    Optional<ShortUrl> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT s FROM ShortUrl s JOIN FETCH s.originalUrl WHERE s.code = :code")
    Optional<ShortUrl> findByCodeWithOriginalUrl(String code);

    List<ShortUrl> findByUserId(UUID userId);
}
