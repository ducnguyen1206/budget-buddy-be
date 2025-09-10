package com.budget.buddy.user.application.service.auth.impl;

import com.budget.buddy.core.config.exception.AuthException;
import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.utils.ApplicationUtil;
import com.budget.buddy.core.utils.JwtUtil;
import com.budget.buddy.core.dto.SendVerificationEmailEvent;
import com.budget.buddy.user.application.constant.UserApplicationConstant;
import com.budget.buddy.user.application.dto.LoginResponse;
import com.budget.buddy.user.application.dto.ResetPasswordRequest;
import com.budget.buddy.user.application.service.auth.AuthenticationService;
import com.budget.buddy.user.domain.model.Session;
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

        userData.findSessionByUserId(user.getId()).ifPresent(userData::deleteSession);

        String token = jwtUtil.generateToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        LoginResponse loginResponse = new LoginResponse(token, refreshToken);
        logger.info("Login successful for email: {}, accessToken generated", email);
        return loginResponse;
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        String email = ApplicationUtil.getEmailFromContext();

        User user = userData.findUserByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Refresh failed: Email {} not found", email);
                    return new AuthException(ErrorCode.LOGIN_FAILED);
                });

        // Validate refresh token with the access token
        if (!jwtUtil.validateToken(refreshToken, email)) {
            logger.warn("Refresh failed: invalid refresh token for {}", email);
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Ensure refresh token matches stored session
        Session session = userData.findSessionByUserId(user.getId())
                .orElseThrow(() -> {
                    logger.warn("Refresh failed: No active session for email {}", email);
                    return new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
                });

        if (!session.getRefreshToken().equals(refreshToken)) {
            logger.warn("Refresh failed: Refresh token mismatch for email {}", email);
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Generate new tokens
        String newAccessToken = jwtUtil.generateToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        session.setRefreshToken(newRefreshToken);
        session.setExpiresAt(LocalDateTime.now()
                .plusSeconds(JwtUtil.REFRESH_TOKEN_EXPIRATION / 1000));
        userData.saveSession(session);

        logger.info("Refresh successful for email: {}", email);
        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout() {
        // Find the user
        User user = retrieveUser();

        // Find and delete the active session if exists
        userData.findSessionByUserId(user.getId())
                .ifPresent(userData::deleteSession);

        logger.info("Logout successful for email: {}", user.getEmailAddress().getValue());
    }

    private User retrieveUser() {
        String email = ApplicationUtil.getEmailFromContext();
        logger.debug("Extracted email from JWT: '{}'", email);

        return userData.findUserByEmail(email)
                .map(user -> {
                    logger.debug("User found in repository: id={}, email='{}'", user.getId(), user.getEmailAddress().getValue());
                    return user;
                })
                .orElseThrow(() -> {
                    logger.error("User not found for email='{}'", email);
                    return new AuthException(ErrorCode.USER_NOT_FOUND);
                });
    }
}
