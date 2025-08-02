package com.budget.buddy.user.application.service.impl;

import com.budget.buddy.BaseIntegrationTest;
import com.budget.buddy.user.application.dto.UserDTO;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findUserByEmailTest() {
        // Arrange
        EmailAddressVO email = new EmailAddressVO("integration2@test.com", true);
        User user = User.builder().emailAddress(email).build();
        userRepository.save(user);

        // Act
        UserDTO result = userService.findUserByEmail(email.getValue());

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.active());
        Assertions.assertEquals(email.getValue(), result.email());
    }
}
