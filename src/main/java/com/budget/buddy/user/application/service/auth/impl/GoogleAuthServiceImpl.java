package com.budget.buddy.user.application.service.auth.impl;

import com.budget.buddy.core.utils.JwtUtil;
import com.budget.buddy.core.utils.RedisTokenService;
import com.budget.buddy.user.application.dto.GoogleTokenResponse;
import com.budget.buddy.user.application.dto.GoogleUserInfo;
import com.budget.buddy.user.application.dto.LoginResponse;
import com.budget.buddy.user.application.service.auth.GoogleAuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final RestClient restClient;


    private static final Logger logger = LogManager.getLogger(GoogleAuthServiceImpl.class);

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.token-uri}")
    private String googleTokenURI;

    @Value("${google.user-info-uri}")
    private String googleUserInfoURI;

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;

    public GoogleAuthServiceImpl(JwtUtil jwtUtil, RedisTokenService redisTokenService) {
        this.restClient = RestClient.create();
        this.jwtUtil = jwtUtil;
        this.redisTokenService = redisTokenService;
    }

    @Override
    public LoginResponse login(String authCode) {
        GoogleTokenResponse tokenResponse = exchangeCodeForToken(authCode);
        GoogleUserInfo userInfo = getUserInfo(tokenResponse.accessToken());
        String email = userInfo.email();

        revokeAccessToken(email);

        String token = jwtUtil.generateToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // Store access token JTI and refresh token in Redis with respective TTLs
        String accessJti = jwtUtil.extractJti(token);
        long accessTtlMs = jwtUtil.getAccessTtlMs();
        redisTokenService.storeAccessJti(email, accessJti, accessTtlMs);
        redisTokenService.setUserAccessJti(email, accessJti, accessTtlMs);
        redisTokenService.storeRefreshToken(email, refreshToken, jwtUtil.getRefreshTtlMs());

        LoginResponse loginResponse = new LoginResponse(token, refreshToken);
        logger.info("Login successful for email: {}, tokens generated and stored in Redis", email);
        return loginResponse;
    }

    private void revokeAccessToken(String email) {
        // Clear existing access JTI for this user (if any) before issuing a new one
        try {
            String existingJti = redisTokenService.getUserAccessJti(email);
            if (existingJti != null) {
                redisTokenService.revokeAccessJti(existingJti);
                redisTokenService.clearUserAccessJti(email);
                logger.debug("Cleared existing JTI for email: {} -> {}", email, existingJti);
            }

        } catch (Exception e) {
            logger.warn("Failed to clear existing JTI for email: {}", email);
        }
    }

    private GoogleUserInfo getUserInfo(String accessToken) {
        return restClient.get()
                .uri(googleUserInfoURI)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfo.class);
    }

    @Override
    public GoogleTokenResponse exchangeCodeForToken(String authCode) {
        // 1. Prepare the parameters for the request
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", "postmessage"); // <--- Magic string for React Popup flow
        params.add("grant_type", "authorization_code");

        // 2. Call Google's API
        return restClient.post()
                .uri(googleTokenURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }
}
