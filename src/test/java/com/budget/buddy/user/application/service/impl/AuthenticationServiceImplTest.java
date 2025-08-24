package com.budget.buddy.user.application.service.impl;

import com.budget.buddy.core.config.exception.AuthException;
import com.budget.buddy.core.config.utils.JwtUtil;
import com.budget.buddy.user.application.dto.LoginResponse;
import com.budget.buddy.user.application.service.auth.impl.AuthenticationServiceImpl;
import com.budget.buddy.user.domain.model.Session;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.service.UserData;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @InjectMocks
    private AuthenticationServiceImpl service;

    @Mock
    private UserData userData;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private User user;

    @Mock
    private Session session;

    @Test
    void generateToken_Existing() {
        when(userData.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(user.getEmailAddress()).thenReturn(new EmailAddressVO("test@test.com", true));
        when(user.getId()).thenReturn(1L);

        service.generateToken("test@test.com");

        verify(userData, times(1)).saveNewUserVerificationToken(any(User.class), any(VerificationTokenVO.class));
    }

    @Test
    void generateToken_New() {
        when(userData.findUserByEmail(anyString())).thenReturn(Optional.empty());
        when(userData.saveNewUser(any(EmailAddressVO.class))).thenReturn(user);
        when(user.getEmailAddress()).thenReturn(new EmailAddressVO("test@test.com", true));
        when(user.getId()).thenReturn(1L);

        service.generateToken("test@test.com");

        verify(userData, times(1)).saveNewUserVerificationToken(any(User.class), any(VerificationTokenVO.class));
    }

    @Test
    void generateToken_With_Exception() {
        when(userData.findUserByEmail(anyString())).thenThrow(new RuntimeException("Test exception"));

        assertThrows(RuntimeException.class, () -> service.generateToken("test@test.com"));
        verify(userData, never()).saveNewUserVerificationToken(any(User.class), any(VerificationTokenVO.class));
    }

    @Test
    void login_With_Valid_User_Should_Generate_Token() {
        // Arrange
        EmailAddressVO emailAddress = new EmailAddressVO("test@test.com", true); // Ensure emailAddress is valid
        String rawPassword = "P@ssword123";
        String encodedPassword = "$2a$10$2j/1m7xtd10ns8b6nxZJ6u8Q2a09qt/odLU1NRJshJDkFlydU9K6e";

        // Mock user repository to return a valid user
        when(userData.findUserByEmail("test@test.com")).thenReturn(Optional.of(user));

        // Mock user details like email address and account status
        when(user.getEmailAddress()).thenReturn(emailAddress);
        when(user.isLocked()).thenReturn(false);

        // Mock password encoding and matching
        when(user.getPassword()).thenReturn(encodedPassword);

        // Mock token generation
        when(jwtUtil.generateToken(eq("test@test.com"), anyList())).thenReturn("token");
        when(jwtUtil.generateRefreshToken("test@test.com")).thenReturn("refreshToken");

        // Act
        LoginResponse response = service.login("test@test.com", rawPassword);

        // Assert
        assertNotNull(response);
        assertEquals("token", response.token());
        assertEquals("refreshToken", response.refreshToken());

        // Verify method calls
        verify(userData, times(1)).findUserByEmail("test@test.com");
        verify(jwtUtil, times(1)).generateToken(eq("test@test.com"), anyList());
        verify(jwtUtil, times(1)).generateRefreshToken("test@test.com");
        verify(userData, times(1)).saveUser(any(User.class));
        verify(userData, times(1)).saveSession(any(Session.class));
    }


    @Test
    void login_With_Invalid_Password_Should_Throw_Exception() {
        when(userData.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(user.getEmailAddress()).thenReturn(new EmailAddressVO("test@test.com", true));
        when(user.isLocked()).thenReturn(false);

        assertThrows(AuthException.class, () -> service.login("test@test.com", "invalidPassword"));
        verify(userData, times(1)).findUserByEmail(anyString());
    }


    @Test
    void login_With_Locked_Account_Should_Throw_Exception() {
        when(userData.findUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(user.getEmailAddress()).thenReturn(new EmailAddressVO("test@test.com", true));
        when(user.isLocked()).thenReturn(true);

        assertThrows(AuthException.class, () -> service.login("test@test.com", "password"));
        verify(userData, times(1)).findUserByEmail(anyString());
    }
}
