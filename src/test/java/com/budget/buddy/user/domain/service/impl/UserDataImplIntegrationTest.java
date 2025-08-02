package com.budget.buddy.user.domain.service.impl;

import com.budget.buddy.BaseIntegrationTest;
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
import java.util.UUID;

class UserDataImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserDataImpl userData;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationRepository userVerificationRepository;

    @Test
    void shouldFindUserByEmail() {
        // Arrange
        EmailAddressVO email = new EmailAddressVO("integration@test.com", true);
        User user = User.builder().emailAddress(email).build();
        userRepository.save(user);

        // Act
        User result = userData.findUserByEmail(email.getValue());

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getEmailAddress().isActive());
        Assertions.assertEquals(email.getValue(), result.getEmailAddress().getValue());
    }

    @Test
    void existsByEmailTest() {
        // Arrange
        EmailAddressVO email = new EmailAddressVO("existsByEmailTest@test.com", true);
        User user = User.builder().emailAddress(email).build();
        userRepository.save(user);

        // Act
        boolean result = userData.existsByEmail(email.getValue());

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void saveNewUserTest() {
        // Arrange
        EmailAddressVO email = new EmailAddressVO("saveUserTest2@test.com", false);

        // Act
        User result = userData.saveNewUser(email);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.getEmailAddress().isActive());
        Assertions.assertEquals(email.getValue(), result.getEmailAddress().getValue());
    }

    @Test
    void saveNewUserVerificationTest() {
        // Arrange
        EmailAddressVO email = new EmailAddressVO("saveUserTest3@test.com", false);
        User user = new User(email, null, 0, false);
        userRepository.save(user);

        VerificationTokenVO verificationToken = new VerificationTokenVO(UUID.randomUUID().toString(), LocalDateTime.now().plusMinutes(30L));

        // Act
        UserVerification result = userData.saveNewUserVerificationToken(user, verificationToken);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getVerificationToken().getValue(), verificationToken.getValue());
        Assertions.assertEquals(verificationToken.getExpiresAt(), result.getVerificationToken().getExpiresAt());
        Assertions.assertEquals(user, result.getUser());
    }
}
