package com.budget.buddy.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt-secret}")
    private String secretKey;

    @Value("${jwt.issuer:}")
    private String issuer;

    @Value("${jwt.kid:}")
    private String keyId;

    @Value("${jwt.access-ttl:30m}")
    private String accessTtl;

    @Value("${jwt.refresh-ttl:7d}")
    private String refreshTtl;

    @Value("${jwt.clock-skew-seconds:30}")
    private long clockSkewSeconds;

    private SecretKey cachedSigningKey;
    private Long cachedAccessTtlMs;
    private Long cachedRefreshTtlMs;

    private SecretKey getSigningKey() {
        if (cachedSigningKey != null) {
            return cachedSigningKey;
        }

        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            cachedSigningKey = key;
            return key;
        } catch (IllegalArgumentException | WeakKeyException e) {
            throw new IllegalStateException("Invalid JWT secret: ensure it is Base64-encoded and at least 256 bits", e);
        }
    }

    public String generateToken(String email) {
        long now = System.currentTimeMillis();

        var builder = Jwts.builder()
                .subject(email)
                .id(generateJti())
                .claim("roles", Collections.emptyList())
                .issuedAt(new Date(now))
                .expiration(new Date(now + getAccessTtlMs()));

        if (!StringUtils.isBlank(issuer)) {
            builder.issuer(issuer);
        }

        if (!StringUtils.isBlank(keyId)) {
            builder.header().add("kid", keyId).and();
        }

        return builder
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        long now = System.currentTimeMillis();

        var builder = Jwts.builder()
                .subject(email)
                .id(generateJti())
                .issuedAt(new Date(now))
                .expiration(new Date(now + getRefreshTtlMs()));

        if (!StringUtils.isBlank(issuer)) {
            builder.issuer(issuer);
        }

        if (!StringUtils.isBlank(keyId)) {
            builder.header().add("kid", keyId).and();
        }

        return builder
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    public String extractJti(String token) {
        return parseClaims(token).getPayload().getId();
    }

    public boolean validateToken(String token, String email) {
        var claims = parseClaims(token).getPayload();

        if (!StringUtils.isBlank(issuer) && !issuer.equals(claims.getIssuer())) {
            return false;
        }

        return email.equals(claims.getSubject()) && !isExpired(claims.getExpiration());
    }

    public boolean validateToken(String token, String email, UserDetails userDetails) {
        return userDetails.getUsername().equals(email) && validateToken(token, email);
    }

    private boolean isExpired(Date expiration) {
        long now = System.currentTimeMillis();
        long skewMs = clockSkewSeconds * 1000L;
        return expiration == null || (expiration.getTime() - skewMs) <= now;
    }

    private Jws<Claims> parseClaims(String token) {
        var parserBuilder = Jwts.parser()
                .verifyWith(getSigningKey())
                .clockSkewSeconds(clockSkewSeconds);

        if (!StringUtils.isBlank(issuer)) {
            parserBuilder.requireIssuer(issuer);
        }

        return parserBuilder
                .build()
                .parseSignedClaims(token);
    }

    private String generateJti() {
        // UUID v4 provides 122 bits of randomness, which is sufficient for JTI uniqueness
        return UUID.randomUUID().toString();
    }

    public long getAccessTtlMs() {
        if (cachedAccessTtlMs != null) {
            return cachedAccessTtlMs;
        }

        long v = parseDurationMillis(accessTtl, 30L * 60 * 1000);
        cachedAccessTtlMs = v;
        return v;
    }

    public long getRefreshTtlMs() {
        if (cachedRefreshTtlMs != null) {
            return cachedRefreshTtlMs;
        }

        long v = parseDurationMillis(refreshTtl, 7L * 24 * 60 * 60 * 1000);
        cachedRefreshTtlMs = v;
        return v;
    }

    private long parseDurationMillis(String value, long defaultMs) {
        if (StringUtils.isBlank(value)) {
            return defaultMs;
        }

        String s = value.trim().toLowerCase();

        try {
            long multiplier = 1000L; // default seconds
            if (s.endsWith("ms")) {
                multiplier = 1L;
                s = s.substring(0, s.length() - 2);
            } else if (s.endsWith("s")) {
                s = s.substring(0, s.length() - 1);
            } else if (s.endsWith("m")) {
                multiplier = 60_000L;
                s = s.substring(0, s.length() - 1);
            } else if (s.endsWith("h")) {
                multiplier = 3_600_000L;
                s = s.substring(0, s.length() - 1);
            } else if (s.endsWith("d")) {
                multiplier = 86_400_000L;
                s = s.substring(0, s.length() - 1);
            }

            double num = Double.parseDouble(s);
            long result = Math.round(num * multiplier);

            if (result <= 0) {
                return defaultMs;
            }

            return result;
        } catch (Exception e) {
            return defaultMs;
        }
    }
}
