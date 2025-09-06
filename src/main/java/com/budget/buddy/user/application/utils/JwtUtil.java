package com.budget.buddy.user.application.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private final HttpServletRequest httpServletRequest;

    public static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    private static final long EXPIRATION_TIME = 3 * 60 * 1000;

    private SecretKey getSigningKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException | WeakKeyException e) {
            throw new IllegalStateException("Invalid JWT secret: ensure it is Base64-encoded and at least 256 bits", e);
        }
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("roles", Collections.emptyList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    public boolean validateToken(String token, String email) {
        var claims = parseClaims(token).getPayload();
        return email.equals(claims.getSubject()) && isExpired(token);
    }

    public boolean validateToken(String token, String email, UserDetails userDetails) {
        return userDetails.getUsername().equals(email) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        return parseClaims(token).getPayload().getExpiration().before(new Date());
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }
}
