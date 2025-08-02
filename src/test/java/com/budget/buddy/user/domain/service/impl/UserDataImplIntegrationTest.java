package com.budget.buddy.user.domain.service.impl;

import com.budget.buddy.BaseIntegrationTest;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserDataImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserDataImpl userData;

    @Autowired
    private UserRepository userRepository;

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
    void saveUserTest() {
        // Arrange
        EmailAddressVO email = new EmailAddressVO("saveUserTest2@test.com", false);

        // Act
        User result = userData.saveUser(email);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.getEmailAddress().isActive());
        Assertions.assertEquals(email.getValue(), result.getEmailAddress().getValue());
    }

//    @Test
//    void saveUserVerificationTest() {
//        // Arrange
//        EmailAddressVO email = new EmailAddressVO("saveUserVerificationTest@test.com", true);
//        User user = User.builder().emailAddress(email).build();
//        userRepository.save(user);
//    }
}
