package com.budget.buddy.user.application.service.auth.impl;

import com.budget.buddy.core.config.exception.AuthException;
import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.dto.SendVerificationEmailEvent;
import com.budget.buddy.core.utils.ApplicationUtil;
import com.budget.buddy.core.utils.JwtUtil;
import com.budget.buddy.core.utils.RedisTokenService;
import com.budget.buddy.user.application.constant.UserApplicationConstant;
import com.budget.buddy.user.application.dto.LoginResponse;
import com.budget.buddy.user.application.dto.ResetPasswordRequest;
import com.budget.buddy.user.application.service.auth.AuthenticationService;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.model.UserVerification;
import com.budget.buddy.user.domain.service.UserData;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LogManager.getLogger(AuthenticationServiceImpl.class);

    private final UserData userData;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;

    @Override
    @Transactional
    public void generateToken(String email) {
        logger.debug("Register attempt initiated for email: {}", email);

        // Check user
        User user = userData.findUserByEmail(email)
                .orElseGet(() -> {
                    EmailAddressVO emailAddress = new EmailAddressVO(email, false);
                    logger.debug("Creating new user with inactive email: {}", email);

                    // Save a new user
                    User savedUser = userData.saveNewUser(emailAddress);
                    logger.info("User successfully registered with email: {}", savedUser.getEmailAddress().getValue());
                    return savedUser;
                });

        // Generate verification toke for register
        LocalDateTime expiresTime = LocalDateTime.now().plusSeconds(UserApplicationConstant.VERIFICATION_EXPIRES_TIME);
        String token = UUID.randomUUID().toString();
        VerificationTokenVO verificationToken = new VerificationTokenVO(token, expiresTime);
        logger.debug("Generated verification token for user {}: {} (expires at {})",
                user.getId(), token, expiresTime);

        userData.saveNewUserVerificationToken(user, verificationToken);
        logger.debug("Verification token saved for user ID: {}", user.getId());

        // Send email (publish messages)
        applicationEventPublisher.publishEvent(
                new SendVerificationEmailEvent(
                        user.getEmailAddress().getValue(),
                        token,
                        expiresTime
                )
        );
        logger.info("Verification email event published for user: {}", user.getEmailAddress().getValue());
    }

    @Override
    public void verifyUser(String token) {
        LocalDateTime now = LocalDateTime.now();

        UserVerification verification = userData.findUserVerificationWithDate(token, now)
                .orElseThrow(() -> {
                    logger.warn("Verification failed: Invalid accessToken {}", token);
                    return new AuthException(ErrorCode.TOKEN_INVALID);
                });

        LocalDateTime newExpiresAt = verification
                .getVerificationToken()
                .getExpiresAt()
                .plusSeconds(UserApplicationConstant.VERIFICATION_EXPIRES_TIME);

        VerificationTokenVO verificationToken = new VerificationTokenVO(token, newExpiresAt);

        verification.setVerificationToken(verificationToken);
        verification.setVerified(true);
        userData.saveUserVerification(verification);

        logger.info("User verified successfully for accessToken: {}", token);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.token();

        if (!request.password().equals(request.reenterPassword())) {
            logger.warn("Password reset failed: Re-entered password does not match for token {}", token);
            throw new BadRequestException(ErrorCode.REENTER_PASSWORD_NOT_THE_SAME);
        }

        UserVerification verification = userData.findUserVerification(token)
                .orElseThrow(() -> {
                    logger.warn("Reset password failed failed: Invalid accessToken {}", token);
                    return new AuthException(ErrorCode.TOKEN_INVALID);
                });

        User user = verification.getUser();
        String email = user.getEmailAddress().getValue();

        EmailAddressVO newEmailVO = new EmailAddressVO(email, true);

        // After reset password, activate user (for new user) and unlocked user (for locked account)
        user.setFailedAttempts(0);
        user.setLocked(false);
        user.setEmailAddress(newEmailVO);
        user.setPassword(passwordEncoder.encode(request.password()));

        userData.saveUser(user);
        userData.deleteUserVerification(verification);
        logger.info("Password updated successfully for {}", email);
    }

    @Override
    public LoginResponse login(String email, String password) {
        User user = userData.findUserByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Login failed: Email {} not found", email);
                    return new AuthException(ErrorCode.LOGIN_FAILED);
                });
        EmailAddressVO emailAddress = user.getEmailAddress();
        if (!emailAddress.isActive()) {
            logger.warn("Login failed: Email {} is not active", email);
            throw new AuthException(ErrorCode.LOGIN_FAILED);
        }

        if (user.isLocked()) {
            logger.warn("Login failed: AccountPayload is locked for email {}", email);
            throw new AuthException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Login failed: Invalid password for email {}", email);

            // Increment failed attempts
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= 5) {
                user.setLocked(true);
                logger.warn("AccountPayload locked due to multiple failed attempts: {}", email);
            }

            userData.saveUser(user);

            throw new AuthException(ErrorCode.LOGIN_FAILED);
        }

        // Reset failed attempts on successful login
        user.setFailedAttempts(0);
        userData.saveUser(user);

        revokeAccessToken(email, null);

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

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // Extract email from the refresh token itself (no auth context required)
        String email = jwtUtil.extractEmail(refreshToken);

        // Basic JWT validation (signature, issuer, expiration)
        if (!jwtUtil.validateToken(refreshToken)) {
            logger.warn("Refresh failed: invalid refresh token for {}", email);
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Validate refresh token existence in Redis for the given user
        if (!redisTokenService.validateRefreshToken(refreshToken, email)) {
            logger.warn("Refresh failed: refresh token not found in Redis or email mismatch for {}", email);
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Rotate: delete the old refresh token and create a new pair
        revokeAccessToken(email, refreshToken);

        String newAccessToken = jwtUtil.generateToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        // Store new tokens
        String newAccessJti = jwtUtil.extractJti(newAccessToken);
        redisTokenService.storeAccessJti(email, newAccessJti, jwtUtil.getAccessTtlMs());
        redisTokenService.storeRefreshToken(email, newRefreshToken, jwtUtil.getRefreshTtlMs());

        logger.info("Refresh successful for email: {}", email);
        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String authHeader) {
        Long userId = ApplicationUtil.getUserIdFromContext();
        User user = userData.findByUserId(userId).orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));
        revokeAccessToken(user.getEmailAddress().getValue(), null);
    }

    private void revokeAccessToken(String email, String refreshToken) {
        // Clear existing access JTI for this user (if any) before issuing a new one
        try {
            String existingJti = redisTokenService.getUserAccessJti(email);
            if (existingJti != null) {
                redisTokenService.revokeAccessJti(existingJti);
                redisTokenService.clearUserAccessJti(email);
                logger.debug("Cleared existing JTI for email: {} -> {}", email, existingJti);
            }

            if (refreshToken != null) {
                redisTokenService.deleteRefreshToken(refreshToken);
            }

        } catch (Exception e) {
            logger.warn("Failed to clear existing JTI for email: {}", email);
        }
    }
}
