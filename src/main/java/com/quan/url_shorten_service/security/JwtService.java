package com.quan.url_shorten_service.security;

import com.quan.url_shorten_service.Entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final byte[] secret;
    private final long ttlSeconds;
    private final ObjectMapper objectMapper;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.ttl-seconds}") long ttlSeconds,
            ObjectMapper objectMapper
    ) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
        this.objectMapper = objectMapper;
    }

    public String createToken(User user) {
        String header = encode(HEADER.getBytes(StandardCharsets.UTF_8));
        String payload = encode(objectMapper.writeValueAsBytes(Map.of(
                "sub", user.getId().toString(),
                "exp", Instant.now().plusSeconds(ttlSeconds).getEpochSecond()
        )));
        String content = header + "." + payload;
        return content + "." + encode(sign(content));
    }

    public UUID parseUserId(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) {
                throw new IllegalArgumentException("Malformed token");
            }

            byte[] suppliedSignature = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(suppliedSignature, expectedSignature)) {
                throw new IllegalArgumentException("Invalid token signature");
            }

            Map<?, ?> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    Map.class
            );
            long expiresAt = ((Number) payload.get("exp")).longValue();
            if (expiresAt <= Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("Token has expired");
            }
            return UUID.fromString((String) payload.get("sub"));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid bearer token", exception);
        }
    }

    private byte[] sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign bearer token", exception);
        }
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
