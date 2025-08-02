package com.budget.buddy.user.application.service.impl;

import com.budget.buddy.user.application.dto.UserDTO;
import com.budget.buddy.user.application.mapper.UserMapper;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.service.UserData;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserData userData;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findUserByEmailTest() {
        // Arrange
        String email = "test_mail@gmail.com";
        EmailAddressVO emailAddressVO = new EmailAddressVO(email, true);

        UserDTO userDTO = new UserDTO(
                1L,
                email,
                emailAddressVO.isActive(),
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        User user = User.builder()
                .emailAddress(emailAddressVO)
                .failedAttempts(0)
                .locked(false)
                .build();

        when(userData.findUserByEmail(email)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDTO);

        UserDTO result = userService.findUserByEmail(email);

        // Act
        Assertions.assertEquals(userDTO, result);
    }
}
