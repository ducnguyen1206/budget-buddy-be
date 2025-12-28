package com.budget.buddy.user.application.service.auth.impl;

import com.budget.buddy.core.config.exception.AuthException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.utils.JwtUtil;
import com.budget.buddy.core.utils.RedisTokenService;
import com.budget.buddy.user.application.dto.GoogleTokenResponse;
import com.budget.buddy.user.application.dto.GoogleUserInfo;
import com.budget.buddy.user.application.dto.LoginResponse;
import com.budget.buddy.user.application.service.auth.GoogleAuthService;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.service.UserData;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
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
    private final UserData userData;

    public GoogleAuthServiceImpl(JwtUtil jwtUtil, RedisTokenService redisTokenService, UserData userData) {
        this.restClient = RestClient.create();
        this.jwtUtil = jwtUtil;
        this.redisTokenService = redisTokenService;
        this.userData = userData;
    }

    @Override
    public LoginResponse login(String authCode) {
        long flowStartNs = System.nanoTime();
        logger.debug("Starting Google login flow");

        GoogleTokenResponse tokenResponse = exchangeCodeForToken(authCode);
        if (tokenResponse == null || tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
            logger.error("Token exchange returned empty response or access token");
            throw new IllegalStateException("Failed to exchange authorization code for access token");
        }

        GoogleUserInfo userInfo = getUserInfo(tokenResponse.accessToken());
        if (userInfo == null || userInfo.email() == null || userInfo.email().isBlank()) {
            logger.error("Google user info response is missing required email");
            throw new IllegalStateException("Failed to retrieve Google user email");
        }
        String email = userInfo.email();
        logger.debug("Retrieved Google user info for email: {}", email);

        // Ensure user exists and status is valid
        User user = userData.findUserByEmail(email).orElseGet(() -> {
            // Create a new user with active email for Google sign-in
            EmailAddressVO emailAddress = new EmailAddressVO(email, true);
            User created = userData.saveNewUser(emailAddress);
            logger.info("Created new user via Google sign-in for email: {} (id: {})", email, created.getId());
            return created;
        });

        // For existing user, validate status
        EmailAddressVO emailAddress = user.getEmailAddress();
        if (!emailAddress.isActive()) {
            logger.warn("Google login failed: Email {} is not active", email);
            throw new AuthException(ErrorCode.LOGIN_FAILED);
        }

        if (user.isLocked()) {
            logger.warn("Google login failed: Account is locked for email {}", email);
            throw new AuthException(ErrorCode.ACCOUNT_LOCKED);
        }

        revokeAccessToken(email);

        String token = jwtUtil.generateToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // Store access token JTI and refresh token in Redis with respective TTLs
        String accessJti = jwtUtil.extractJti(token);
        long accessTtlMs = jwtUtil.getAccessTtlMs();
        long refreshTtlMs = jwtUtil.getRefreshTtlMs();
        logger.debug("Storing tokens in Redis for email: {} (access TTL: {} ms, refresh TTL: {} ms)", email, accessTtlMs, refreshTtlMs);
        redisTokenService.storeAccessJti(email, accessJti, accessTtlMs);
        redisTokenService.setUserAccessJti(email, accessJti, accessTtlMs);
        redisTokenService.storeRefreshToken(email, refreshToken, refreshTtlMs);

        LoginResponse loginResponse = new LoginResponse(token, refreshToken);
        long totalMs = (System.nanoTime() - flowStartNs) / 1_000_000L;
        logger.info("Login successful for email: {} in {} ms; tokens generated and stored in Redis", email, totalMs);
        return loginResponse;
    }

    private void revokeAccessToken(String email) {
        // Clear existing access JTI for this user (if any) before issuing a new one
        try {
            String existingJti = redisTokenService.getUserAccessJti(email);
            if (existingJti != null && !existingJti.isBlank()) {
                redisTokenService.revokeAccessJti(existingJti);
                redisTokenService.clearUserAccessJti(email);
                logger.debug("Cleared existing JTI for email: {} -> {}", email, existingJti);
            }

        } catch (Exception e) {
            logger.warn("Failed to clear existing JTI for email: {}", email, e);
        }
    }

    private GoogleUserInfo getUserInfo(String accessToken) {
        long startNs = System.nanoTime();
        logger.debug("Requesting Google user info");
        try {
            GoogleUserInfo info = restClient.get()
                    .uri(googleUserInfoURI)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(GoogleUserInfo.class);
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
            logger.debug("Google user info request completed in {} ms", elapsedMs);
            return info;
        } catch (Exception e) {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
            logger.error("Failed to fetch Google user info ({} ms)", elapsedMs, e);
            throw new IllegalStateException("Failed to fetch Google user info", e);
        }
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
        long startNs = System.nanoTime();
        logger.debug("Exchanging Google auth code for tokens");
        try {
            GoogleTokenResponse response = restClient.post()
                    .uri(googleTokenURI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(GoogleTokenResponse.class);
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
            logger.debug("Token exchange completed in {} ms", elapsedMs);
            return response;
        } catch (Exception e) {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
            logger.error("Failed to exchange auth code for token ({} ms)", elapsedMs, e);
            throw new IllegalStateException("Failed to exchange auth code for token", e);
        }
    }
}
