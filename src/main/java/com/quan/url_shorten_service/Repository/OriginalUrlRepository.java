package com.quan.url_shorten_service.Repository;

import com.quan.url_shorten_service.Entity.OriginalUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OriginalUrlRepository extends JpaRepository<OriginalUrl, UUID> {

    Optional<OriginalUrl> findByUrl(String url);

    boolean existsByUrl(String url);
}
