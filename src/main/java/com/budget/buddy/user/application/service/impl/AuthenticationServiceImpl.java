package com.budget.buddy.user.application.service.impl;

import com.budget.buddy.core.config.exception.AuthException;
import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.core.event.SendVerificationEmailEvent;
import com.budget.buddy.user.application.constant.UserApplicationConstant;
import com.budget.buddy.user.application.dto.LoginResponse;
import com.budget.buddy.user.application.dto.ResetPasswordRequest;
import com.budget.buddy.user.application.service.AuthenticationService;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void registerUser(String email) {
        logger.debug("Register attempt initiated for email: {}", email);

        // Check user
        if (userData.existsByEmail(email)) {
            logger.warn("Registration failed: Email '{}' already exists", email);
            throw new AuthException(ErrorCode.EMAIL_EXISTS);
        }

        EmailAddressVO emailAddress = new EmailAddressVO(email, false);
        logger.debug("Creating new user with inactive email: {}", email);

        // Save new user
        User user = userData.saveNewUser(emailAddress);
        logger.info("User successfully registered with email: {}", user.getEmailAddress().getValue());

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
        String email = request.email();

        if (!request.password().equals(request.reenterPassword())) {
            logger.warn("Password reset failed: Re-entered password does not match for user {}", email);
            throw new BadRequestException(ErrorCode.REENTER_PASSWORD_NOT_THE_SAME);
        }

        User user = userData.findUserByEmail(email).orElseThrow(() -> {
            logger.warn("Password reset failed: Email not found for {}", email);
            return new NotFoundException(ErrorCode.EMAIL_NOT_FOUND);
        });

        UserVerification verification = userData.findUserVerificationByUserId(user.getId()).orElseThrow(() -> {
            logger.warn("Password reset failed: User has not been verified yet {}", email);
            return new AuthException(ErrorCode.USER_HAS_NOT_BEEN_VERIFIED);
        });

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
        if (emailAddress.isActive()) {
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
        return null;
    }
}
