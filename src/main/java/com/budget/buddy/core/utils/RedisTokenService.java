package com.budget.buddy.core.utils;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Simple Redis-backed token store for access JTIs and refresh tokens.
 * Keys:
 * - access: jti:{jti} -> email (TTL = access token lifetime)
 * - refresh:{sha256(token)} -> email (TTL = refresh token lifetime)
 * - user:accessJti:{email} -> jti (TTL = access token lifetime)
 */
@Service
@RequiredArgsConstructor
public class RedisTokenService {
    private static final Logger logger = LogManager.getLogger(RedisTokenService.class);

    private final StringRedisTemplate redis;
    private static final String ACCESS_TOKEN_PROPERTY = "access:jti:";
    private static final String REFRESH_TOKEN_PROPERTY = "refresh:";
    private static final String USER_ACCESS_JTI_PROPERTY = "user:accessJti:";

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public void storeAccessJti(String email, String jti, long ttlMs) {
        if (jti == null) return;
        String key = ACCESS_TOKEN_PROPERTY + jti;
        redis.opsForValue().set(key, email, ttlMs, TimeUnit.MILLISECONDS);
    }

    public boolean isAccessJtiActive(String jti) {
        if (jti == null) return false;
        String key = ACCESS_TOKEN_PROPERTY + jti;
        Boolean active = redis.hasKey(key);
        return Optional.ofNullable(active).orElse(false);
    }

    public void revokeAccessJti(String jti) {
        if (jti == null) return;
        String key = ACCESS_TOKEN_PROPERTY + jti;
        redis.delete(key);
    }

    public void storeRefreshToken(String email, String refreshToken, long ttlMs) {
        String key = REFRESH_TOKEN_PROPERTY + hashToken(refreshToken);
        redis.opsForValue().set(key, email, ttlMs, TimeUnit.MILLISECONDS);
    }

    public boolean validateRefreshToken(String refreshToken, String expectedEmail) {
        String key = REFRESH_TOKEN_PROPERTY + hashToken(refreshToken);
        String email = redis.opsForValue().get(key);
        return email != null && email.equals(expectedEmail);
    }

    public void deleteRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PROPERTY + hashToken(refreshToken);
        redis.delete(key);
    }

    // Map the current access JTI for a user (by email). TTL should match access token lifetime
    public void setUserAccessJti(String email, String jti, long ttlMs) {
        if (email == null || jti == null) return;
        String key = USER_ACCESS_JTI_PROPERTY + email;
        redis.opsForValue().set(key, jti, ttlMs, TimeUnit.MILLISECONDS);
    }

    public String getUserAccessJti(String email) {
        if (email == null) return null;
        String key = USER_ACCESS_JTI_PROPERTY + email;
        return redis.opsForValue().get(key);
    }

    public void clearUserAccessJti(String email) {
        if (email == null) return;
        String key = USER_ACCESS_JTI_PROPERTY + email;
        redis.delete(key);
    }
}
