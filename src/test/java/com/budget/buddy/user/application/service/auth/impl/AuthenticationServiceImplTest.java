package com.budget.buddy.user.application.service.auth.impl;

import com.budget.buddy.core.config.exception.AuthException;
import com.budget.buddy.core.dto.SendVerificationEmailEvent;
import com.budget.buddy.core.utils.JwtUtil;
import com.budget.buddy.core.utils.RedisTokenService;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.service.UserData;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {

    @Mock
    private UserData userData;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTokenService redisTokenService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void shouldGenerateTokenForExistingUser() {
        // Arrange
        String email = "existinguser@example.com";
        User existingUser = new User(new EmailAddressVO(email, true), null, 0, false);
        existingUser.setId(1L);
        existingUser.setEmailAddress(new EmailAddressVO(email, true));
        when(userData.findUserByEmail(email)).thenReturn(Optional.of(existingUser));

        ArgumentCaptor<VerificationTokenVO> verificationTokenCaptor = ArgumentCaptor.forClass(VerificationTokenVO.class);

        // Act
        authenticationService.generateToken(email);

        // Assert
        verify(userData, never()).saveNewUser(any());
        verify(userData).saveNewUserVerificationToken(eq(existingUser), verificationTokenCaptor.capture());
        verify(applicationEventPublisher).publishEvent(any(SendVerificationEmailEvent.class));

        VerificationTokenVO capturedToken = verificationTokenCaptor.getValue();
        assertNotNull(capturedToken);
        assertNotNull(capturedToken.getValue());
        assertTrue(capturedToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void shouldGenerateTokenForNewUser() {
        // Arrange
        String email = "newuser@example.com";
        User newUser = new User(new EmailAddressVO(email, true), null, 0, false);
        newUser.setId(2L);
        newUser.setEmailAddress(new EmailAddressVO(email, false));
        when(userData.findUserByEmail(email)).thenReturn(Optional.empty());
        when(userData.saveNewUser(any(EmailAddressVO.class))).thenReturn(newUser);

        ArgumentCaptor<VerificationTokenVO> verificationTokenCaptor = ArgumentCaptor.forClass(VerificationTokenVO.class);

        // Act
        authenticationService.generateToken(email);

        // Assert
        verify(userData).saveNewUser(any(EmailAddressVO.class));
        verify(userData).saveNewUserVerificationToken(eq(newUser), verificationTokenCaptor.capture());
        verify(applicationEventPublisher).publishEvent(any(SendVerificationEmailEvent.class));

        VerificationTokenVO capturedToken = verificationTokenCaptor.getValue();
        assertNotNull(capturedToken);
        assertNotNull(capturedToken.getValue());
        assertTrue(capturedToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void shouldThrowExceptionWhenEventPublishingFails() {
        // Arrange
        String email = "user@example.com";
        User user = new User(new EmailAddressVO(email, true), null, 0, false);
        user.setId(3L);
        user.setEmailAddress(new EmailAddressVO(email, true));
        when(userData.findUserByEmail(email)).thenReturn(Optional.of(user));

        doThrow(new RuntimeException("Event publishing failed")).when(applicationEventPublisher).publishEvent(any(SendVerificationEmailEvent.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.generateToken(email));
    }

    @Test
    void shouldNotGenerateTokenIfEmailIsInvalid() {
        // Arrange
        String email = "invalid-email";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.generateToken(email));
        verify(userData, times(0)).saveNewUser(any());
        verify(userData, times(0)).saveNewUserVerificationToken(any(), any());
        verify(applicationEventPublisher, times(0)).publishEvent(any());
    }
}