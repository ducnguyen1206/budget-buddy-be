package com.budget.buddy.user.domain.service.impl;

import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDataImplTest {

    @InjectMocks
    private UserDataImpl userData;

    @Mock
    private UserRepository userRepository;

    @Test
    void findUserByEmailTest() {
        // Arrange
        String email = "test_email@gmail.com";
        EmailAddressVO emailAddressVO = new EmailAddressVO(email, true);
        User user = User.builder().emailAddress(emailAddressVO).build();

        when(userRepository.findByEmailAddress_Value(email)).thenReturn(Optional.of(user));

        // Act
        User result = userData.findUserByEmail(email);

        // Assert
        Assertions.assertEquals(user, result);
        Assertions.assertTrue(result.getEmailAddress().isActive());
        Assertions.assertEquals(email, result.getEmailAddress().getValue());
    }

    @Test
    void existsByEmailTest() {
        // Arrange
        String email = "testEmail2@gmail.com";
        when(userRepository.existsByEmailAddress_Value(email)).thenReturn(true);

        // Act
        boolean result = userData.existsByEmail(email);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    void saveUserTest() {
        // Arrange
        EmailAddressVO email = new EmailAddressVO("saveUserTest@test.com", false);
        User user = User.builder().emailAddress(email).build();
        when(userRepository.save(any())).thenReturn(user);

        // Act
        User result = userData.saveUser(email);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user, result);
        Assertions.assertFalse(result.getEmailAddress().isActive());
        Assertions.assertEquals(email.getValue(), result.getEmailAddress().getValue());
    }
}
