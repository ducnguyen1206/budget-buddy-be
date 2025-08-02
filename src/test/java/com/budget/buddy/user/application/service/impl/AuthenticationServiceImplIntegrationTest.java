package com.budget.buddy.user.application.service.impl;

import com.budget.buddy.BaseIntegrationTest;
import com.budget.buddy.user.application.constant.UserApplicationConstant;
import com.budget.buddy.user.application.exception.AuthException;
import com.budget.buddy.user.application.exception.UserErrorCode;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.model.UserVerification;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;
import com.budget.buddy.user.infrastructure.repository.UserRepository;
import com.budget.buddy.user.infrastructure.repository.UserVerificationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

class AuthenticationServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthenticationServiceImpl authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationRepository userVerificationRepository;

    @Test
    void registerUserHappyCase() {
        // Arrange
        String email = "registerUserHappyCase@gmail.com";
        EmailAddressVO emailAddress = new EmailAddressVO(email, false);

        // Act
        authenticationService.registerUser(email);

        Optional<User> registeredUserOptional = userRepository.findByEmailAddress_Value(emailAddress.getValue());

        // Assert
        Assertions.assertTrue(registeredUserOptional.isPresent());

        User user = registeredUserOptional.get();
        Assertions.assertEquals(emailAddress.getValue(), user.getEmailAddress().getValue());
        Assertions.assertFalse(user.getEmailAddress().isActive());

        Optional<UserVerification> userVerificationOptional = userVerificationRepository.findByUserId(user.getId());
        Assertions.assertTrue(userVerificationOptional.isPresent());

        UserVerification userVerification = userVerificationOptional.get();
        Assertions.assertFalse(userVerification.isVerified());
    }


    @Test
    void registerUserExists() {
        // Arrange
        String email = "registerUserExists@gmail.com";
        EmailAddressVO emailAddress = new EmailAddressVO(email, false);
        User user = new User(emailAddress, null, 0, false);
        userRepository.save(user);

        // Act
        AuthException authException = Assertions.assertThrows(AuthException.class, () -> authenticationService.registerUser(email));

        // Assert
        Assertions.assertNotNull(authException);
        Assertions.assertEquals(UserErrorCode.EMAIL_EXISTS.getCode(), authException.getErrorCode());
    }

    @Test
    void verifyUserTestHappyCase() {
        // Arrange
        LocalDateTime expiresTime = LocalDateTime.now().plusSeconds(UserApplicationConstant.VERIFICATION_EXPIRES_TIME);
        String token = UUID.randomUUID().toString();
        VerificationTokenVO verificationToken = new VerificationTokenVO(token, expiresTime);

        String email = "verifyUserTestHappyCase@gmail.com";
        EmailAddressVO emailAddress = new EmailAddressVO(email, false);
        User user = new User(emailAddress, null, 0, false);
        userRepository.save(user);

        UserVerification verification = new UserVerification(user, verificationToken, false);

        userVerificationRepository.save(verification);

        // Act
        authenticationService.verifyUser(token);

        // Assert
        Optional<UserVerification> userVerification = userVerificationRepository.findByUserId(user.getId());
        Assertions.assertTrue(userVerification.isPresent());
        Assertions.assertTrue(userVerification.get().isVerified());
    }

    @Test
    void verifyUserTestHappyCaseTokenExpires() {
        // Arrange
        LocalDateTime expiresTime = LocalDateTime.now().minusSeconds(UserApplicationConstant.VERIFICATION_EXPIRES_TIME);
        String token = UUID.randomUUID().toString();
        VerificationTokenVO verificationToken = new VerificationTokenVO(token, expiresTime);

        String email = "verifyUserTestHappyCaseTokenExpires@gmail.com";
        EmailAddressVO emailAddress = new EmailAddressVO(email, false);
        User user = new User(emailAddress, null, 0, false);
        userRepository.save(user);

        UserVerification verification = new UserVerification(user, verificationToken, false);

        userVerificationRepository.save(verification);

        // Act
        AuthException authException = Assertions.assertThrows(AuthException.class, () -> authenticationService.verifyUser(token));

        // Assert
        Assertions.assertNotNull(authException);
        Assertions.assertEquals(UserErrorCode.TOKEN_INVALID.getCode(), authException.getErrorCode());
    }

    @Test
    void verifyUserTestTokenNotExists() {
        // Arrange
        LocalDateTime expiresTime = LocalDateTime.now().plusSeconds(UserApplicationConstant.VERIFICATION_EXPIRES_TIME);
        String token = UUID.randomUUID().toString();
        VerificationTokenVO verificationToken = new VerificationTokenVO(token, expiresTime);

        String email = "verifyUserTestTokenNotExists@gmail.com";
        EmailAddressVO emailAddress = new EmailAddressVO(email, false);
        User user = new User(emailAddress, null, 0, false);
        userRepository.save(user);

        UserVerification verification = new UserVerification(user, verificationToken, false);

        userVerificationRepository.save(verification);

        // Act
        AuthException authException = Assertions.assertThrows(AuthException.class, () -> authenticationService.verifyUser(UUID.randomUUID().toString()));

        // Assert
        Assertions.assertNotNull(authException);
        Assertions.assertEquals(UserErrorCode.TOKEN_INVALID.getCode(), authException.getErrorCode());
    }
}
