package com.budget.buddy.user.application.service.impl;

import com.budget.buddy.core.event.SendVerificationEmailEvent;
import com.budget.buddy.user.application.service.AuthenticationService;
import com.budget.buddy.user.application.constant.UserApplicationConstant;
import com.budget.buddy.user.application.exception.AuthException;
import com.budget.buddy.user.application.exception.UserErrorCode;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.service.UserData;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LogManager.getLogger(AuthenticationServiceImpl.class);

    private final UserData userData;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void registerUser(String email) {
        logger.debug("Register attempt initiated for email: {}", email);

        // Check user
        if (userData.existsByEmail(email)) {
            logger.warn("Registration failed: Email '{}' already exists", email);
            throw new AuthException(UserErrorCode.EMAIL_EXISTS);
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
}
