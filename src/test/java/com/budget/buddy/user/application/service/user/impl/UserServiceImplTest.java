package com.budget.buddy.user.application.service.user.impl;

import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.user.application.dto.UserDTO;
import com.budget.buddy.user.application.mapper.UserMapper;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.service.UserData;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserData userData;

    @Mock
    private UserMapper userMapper;

    @Test
    void shouldFindUserIdByEmail() {
        // given
        User user = User.builder().build();
        user.setId(1L);
        user.setEmailAddress(new EmailAddressVO("test@test.com", true));
        when(userData.findActiveUser(anyString())).thenReturn(Optional.of(user));

        UserDTO dto = new UserDTO(1L, "test@test.com", true, false, LocalDateTime.now(), LocalDateTime.now());

        // when
        Long result = userService.findUserIdByEmail("test@test.com");

        // then
        assertEquals(1L, result);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenEmailNotFound() {
        // given
        when(userData.findActiveUser(anyString())).thenReturn(Optional.empty());

        // when + then
        assertThrows(NotFoundException.class, () -> userService.findUserIdByEmail("test@test.com"));
    }
}