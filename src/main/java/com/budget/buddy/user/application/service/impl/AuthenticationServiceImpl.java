package com.budget.buddy.user.application.service.impl;

import com.budget.buddy.core.config.exception.AuthException;
import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.utils.JwtUtil;
import com.budget.buddy.core.event.SendVerificationEmailEvent;
import com.budget.buddy.user.application.constant.UserApplicationConstant;
import com.budget.buddy.user.application.dto.LoginResponse;
import com.budget.buddy.user.application.dto.ResetPasswordRequest;
import com.budget.buddy.user.application.service.AuthenticationService;
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
import java.util.Collections;
import java.util.UUID;

/**
 * Implementation of the {@link AuthenticationService} interface providing methods for authentication,
 * user verification, password reset, and session management.
 * This service includes functionalities such as:
 * - Generating and verifying user tokens
 * - Managing user authentication and authorization processes
 * - Handling password reset requests
 * - Managing user sessions, including login, logout, and token refresh mechanisms
 *
 * Uses {@link UserData} for data access and persistence operations.
 * Leverages {@link JwtUtil} for generating and validating JSON Web Tokens (JWT).
 * Utilizes {@link BCryptPasswordEncoder} for password encoding.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LogManager.getLogger(AuthenticationServiceImpl.class);

    private final UserData userData;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;

    /**
     * Generates a verification token for a given user email. If the user does not exist,
     * a new user is created with an inactive email. Once the token is generated, it is saved
     * and a verification email event is published for the user.
     *
     * @param email the email address of the user for which the token should be generated
     */
    @Override
    @Transactional
    public void generateToken(String email) {
        logger.debug("Register attempt initiated for email: {}", email);

        // Check user
        User user = userData.findUserByEmail(email)
                .orElseGet(() -> {
                    EmailAddressVO emailAddress = new EmailAddressVO(email, false);
                    logger.debug("Creating new user with inactive email: {}", email);

                    // Save new user
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

    /**
     * Verifies the user based on the provided access token. If the token is valid and
     * not expired, the associated user record is updated to indicate successful verification.
     * The verification token's expiration time is extended, and the verification record
     * is saved to the database.
     *
     * @param token the access token used to verify the user
     * @throws AuthException if the token is invalid or expired
     */
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

    /**
     * Resets the password for a user based on the provided request. This operation checks if the
     * re-entered password matches the new password and validates the provided token. Upon success,
     * resets the password, unlocks the user's account (if locked), activates the email (if inactive),
     * and updates the user record in the database. Deletes the associated verification record after
     * successful password reset.
     *
     * @param request the password reset request containing the new password, re-entered password,
     *                and verification token
     * @throws BadRequestException if the new password and re-entered password do not match
     * @throws AuthException if the provided token is invalid or does not exist
     */
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

    /**
     * Authenticates a user based on the provided email and password.
     * If the authentication is successful, a new session is created along with JWT access and refresh tokens.
     * If the authentication fails, appropriate errors are logged and exceptions are thrown.
     *
     * @param email the email address of the user attempting to log in
     * @param password the password associated with the user's account
     * @return a {@code LoginResponse} containing the access token and refresh token for the authenticated user
     * @throws AuthException if the email is not found, the email is inactive, the account is locked, or the password is incorrect
     */
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

        // Generate JWT with roles (default to "USER" for now)
        String token = jwtUtil.generateToken(email, Collections.singletonList("USER"));
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // Create and save session
        Session session = new Session();
        session.setUser(user);
        session.setToken(token);
        session.setRefreshToken(refreshToken);
        // 3) expiry (JwtUtil.REFRESH_TOKEN_EXPIRATION is in ms; LocalDateTime has no plusMillis)
        session.setExpiresAt(LocalDateTime.now()
                .plusSeconds(JwtUtil.REFRESH_TOKEN_EXPIRATION / 1000));
        userData.saveSession(session);

        LoginResponse loginResponse = new LoginResponse(token, refreshToken);
        logger.info("Login successful for email: {}, accessToken generated", email);
        return loginResponse;
    }

    /**
     * Refreshes the authentication tokens for a user based on the provided refresh token.
     * This method validates the provided refresh token, ensures it matches the active session,
     * and generates new access and refresh tokens. The user's session is updated with the latest tokens.
     *
     * @param refreshToken the refresh token provided by the client for token renewal
     * @return a {@code LoginResponse} containing the newly generated access token and refresh token
     * @throws AuthException if the refresh token is invalid, expired, mismatched, or if the user information cannot be found
     */
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // Extract email from the refresh token
        String email;
        try {
            email = jwtUtil.extractEmail(refreshToken);
        } catch (Exception e) {
            logger.warn("Refresh failed: unable to parse refresh token");
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Validate refresh token with the extracted email
        if (!jwtUtil.validateToken(refreshToken, email)) {
            logger.warn("Refresh failed: invalid refresh token for {}", email);
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Get user
        User user = userData.findUserByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Refresh failed: Email {} not found", email);
                    return new AuthException(ErrorCode.LOGIN_FAILED);
                });

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
        String newAccessToken = jwtUtil.generateToken(email, Collections.singletonList("USER"));
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        // Update session
        session.setToken(newAccessToken);
        session.setRefreshToken(newRefreshToken);
        session.setExpiresAt(LocalDateTime.now()
                .plusSeconds(JwtUtil.REFRESH_TOKEN_EXPIRATION / 1000));
        userData.saveSession(session);

        logger.info("Refresh successful for email: {}", email);
        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    /**
     * Logs out a user by invalidating their active session, if it exists. If the user is not found,
     * an exception is thrown. This operation ensures that any active session associated with the
     * user is properly removed to prevent further access.
     *
     * @param email the email address of the user to log out
     * @throws AuthException if the user associated with the given email does not exist
     */
    @Override
    public void logout(String email) {
        // Find the user
        User user = userData.findUserByEmail(email)
                .orElseThrow(() -> new AuthException(ErrorCode.EMAIL_NOT_FOUND));

        // Find and delete the active session if exists
        userData.findSessionByUserId(user.getId())
                .ifPresent(userData::deleteSession);

        logger.info("Logout successful for email: {}", email);
    }
}
