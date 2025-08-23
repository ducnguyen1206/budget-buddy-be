package com.budget.buddy.user.application.service.impl;

import com.budget.buddy.BaseIntegrationTest;
import com.budget.buddy.core.config.exception.AuthException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.utils.JwtUtil;
import com.budget.buddy.user.application.constant.UserApplicationConstant;
import com.budget.buddy.user.application.dto.LoginResponse;
import com.budget.buddy.user.application.dto.ResetPasswordRequest;
import com.budget.buddy.user.domain.model.Session;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.model.UserVerification;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;
import com.budget.buddy.user.infrastructure.repository.SessionRepository;
import com.budget.buddy.user.infrastructure.repository.UserRepository;
import com.budget.buddy.user.infrastructure.repository.UserVerificationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class AuthenticationServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthenticationServiceImpl authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationRepository userVerificationRepository;

    @Autowired private SessionRepository sessionRepository;
    @Autowired private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private User persistUser(String email, boolean active, String rawPassword, int failedAttempts, boolean locked) {
        return userRepository.save(new User(new EmailAddressVO(email, active),
                encoder.encode(rawPassword), failedAttempts, locked));
    }

    private Session persistSession(User user) {
        Session s = new Session();
        s.setUser(user);
        s.setToken("old-access-" + UUID.randomUUID());
        s.setRefreshToken("old-refresh-" + UUID.randomUUID());
        s.setExpiresAt(LocalDateTime.now().plusDays(1));
        return sessionRepository.save(s);
    }

    @Test
    void generateTokenUserNotExists() {
        // Arrange
        String email = "registerUserHappyCase@gmail.com";
        EmailAddressVO emailAddress = new EmailAddressVO(email, false);

        // Act
        authenticationService.generateToken(email);

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
    void generateTokenUserExists() {
        // Arrange
        String email = "registerUserExists@gmail.com";
        EmailAddressVO emailAddress = new EmailAddressVO(email, true);
        User user = new User(emailAddress, null, 0, false);
        userRepository.save(user);

        // Act
        authenticationService.generateToken(email);

        Optional<User> registeredUserOptional = userRepository.findByEmailAddress_Value(emailAddress.getValue());

        // Assert
        Assertions.assertTrue(registeredUserOptional.isPresent());
        Assertions.assertTrue(registeredUserOptional.get().getEmailAddress().isActive());
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
        Assertions.assertEquals(ErrorCode.TOKEN_INVALID.getCode(), authException.getErrorCode());
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
        Assertions.assertEquals(ErrorCode.TOKEN_INVALID.getCode(), authException.getErrorCode());
    }

    @Test
    void resetPasswordTest() {
        // Arrange
        String email = "resetPasswordTest@gmail.com";
        User user = saveUser(email);
        saveUserVerification(user);

        ResetPasswordRequest request = new ResetPasswordRequest(email, "1234", "1234");

        // Act
        authenticationService.resetPassword(request);

        // Assert
        Assertions.assertTrue(userVerificationRepository.findByUserId(user.getId()).isEmpty());
        Optional<User> savedUser = userRepository.findById(user.getId());
        Assertions.assertTrue(savedUser.isPresent());
        Assertions.assertTrue(savedUser.get().getEmailAddress().isActive());
    }

    @Test
    void login_success_deletesOldSession_createsNew_resetsAttempts_andReturnsValidTokens() {
        // Arrange
        String email = "login.success@test.com";
        User user = persistUser(email, true, "pw", 2, false); // had failed attempts
        Session old = persistSession(user);                   // should be deleted

        // Act
        LoginResponse res = authenticationService.login(email, "pw");

        // Assert tokens
        Assertions.assertNotNull(res);
        Assertions.assertFalse(res.token().isEmpty());
        Assertions.assertFalse(res.refreshToken().isEmpty());
        Assertions.assertTrue(jwtUtil.validateToken(res.token(), email));

        // Assert failed attempts reset
        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        Assertions.assertEquals(0, reloaded.getFailedAttempts());

        // Assert old session deleted, new one saved
        Assertions.assertTrue(sessionRepository.findById(old.getId()).isEmpty());
        Optional<Session> newSession = sessionRepository.findByUserId(user.getId());
        Assertions.assertTrue(newSession.isPresent());
        Assertions.assertEquals(res.refreshToken(), newSession.get().getRefreshToken());
    }

    @Test
    void login_fails_whenEmailNotFound() {
        AuthException ex = Assertions.assertThrows(AuthException.class,
                () -> authenticationService.login("missing@test.com", "pw"));
        Assertions.assertEquals(ErrorCode.LOGIN_FAILED.getCode(), ex.getErrorCode());
    }

    @Test
    void login_fails_whenEmailInactive() {
        String email = "inactive@test.com";
        persistUser(email, false, "pw", 0, false);

        AuthException ex = Assertions.assertThrows(AuthException.class,
                () -> authenticationService.login(email, "pw"));
        Assertions.assertEquals(ErrorCode.LOGIN_FAILED.getCode(), ex.getErrorCode());
    }

    @Test
    void login_fails_whenAccountLocked() {
        String email = "locked@test.com";
        persistUser(email, true, "pw", 0, true);

        AuthException ex = Assertions.assertThrows(AuthException.class,
                () -> authenticationService.login(email, "pw"));
        Assertions.assertEquals(ErrorCode.ACCOUNT_LOCKED.getCode(), ex.getErrorCode());
    }

    @Test
    void login_wrongPassword_incrementsFailedAttempts_andLocksAtFive() {
        String email = "wrongpw@test.com";
        User user = persistUser(email, true, "pw", 4, false); // next fail hits lock

        AuthException ex = Assertions.assertThrows(AuthException.class,
                () -> authenticationService.login(email, "bad"));
        Assertions.assertEquals(ErrorCode.LOGIN_FAILED.getCode(), ex.getErrorCode());

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        Assertions.assertTrue(reloaded.isLocked());
        Assertions.assertEquals(5, reloaded.getFailedAttempts());
    }

    // ✅ Success: rotates tokens, updates session, returns valid tokens
    @Test
    void refreshToken_success_rotatesTokens_updatesSession_andReturnsTokens() {
        String email = "refresh.successSuccess@test.com";
        User user = persistUser(email, true, "pw", 0, false);

        // Seed an active session with a valid stored refresh token
        String storedRefresh = jwtUtil.generateRefreshToken(email);
        Session s = new Session();
        s.setUser(user);
        s.setToken("old-access2");
        s.setRefreshToken(storedRefresh);
        s.setExpiresAt(LocalDateTime.now().plusDays(1));
        sessionRepository.save(s);

        // Act
        LoginResponse res = authenticationService.refreshToken(storedRefresh);

        // Assert: response
        Assertions.assertNotNull(res);
        Assertions.assertNotNull(res.token());
        Assertions.assertFalse(res.token().isEmpty());
        Assertions.assertNotNull(res.refreshToken());
        Assertions.assertFalse(res.refreshToken().isEmpty());

        // Assert: new access token is valid for same email
        Assertions.assertTrue(jwtUtil.validateToken(res.token(), email));

        // Assert: session rotated & persisted
        Session after = sessionRepository.findByUserId(user.getId()).orElseThrow();
        Assertions.assertEquals(res.token(), after.getToken());
        Assertions.assertEquals(res.refreshToken(), after.getRefreshToken());
        Assertions.assertEquals(storedRefresh, res.refreshToken());
        Assertions.assertTrue(after.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    // ❌ Provided refresh token valid but doesn't match stored one -> AUTH_007 INVALID_REFRESH_TOKEN
    @Test
    void refreshToken_fails_whenTokenDoesNotMatchStoredSession() {
        String email = "refresh.mismatch@test.com";
        User user = persistUser(email, true, "pw", 0, false);

        // Store one refresh token...
        String storedRefresh = jwtUtil.generateRefreshToken("refresh2.mismatch@test.com");
        Session s = new Session();
        s.setUser(user);
        s.setToken("old-access");
        s.setRefreshToken(storedRefresh);
        s.setExpiresAt(LocalDateTime.now().plusDays(1));
        sessionRepository.save(s);

        // ...but provide a different (still valid) one
        String providedDifferentRefresh = jwtUtil.generateRefreshToken(email);

        AuthException ex = Assertions.assertThrows(
                AuthException.class,
                () -> authenticationService.refreshToken(providedDifferentRefresh)
        );
        Assertions.assertEquals(ErrorCode.INVALID_REFRESH_TOKEN.getCode(), ex.getErrorCode());
    }

    // ❌ Malformed token -> AUTH_007 INVALID_REFRESH_TOKEN
    @Test
    void refreshToken_fails_whenTokenIsMalformed() {
        AuthException ex = Assertions.assertThrows(
                AuthException.class,
                () -> authenticationService.refreshToken("not-a-jwt")
        );
        Assertions.assertEquals(ErrorCode.INVALID_REFRESH_TOKEN.getCode(), ex.getErrorCode());
    }

    @Test
    void logout_success_deletesActiveSession() {
        // Arrange
        String email = "logout.success@test.com";
        User user = persistUser(email, true, "pw", 0, false);

        Session session = new Session();
        session.setUser(user);
        session.setToken(jwtUtil.generateToken(email, List.of("USER")));
        session.setRefreshToken(jwtUtil.generateRefreshToken(email));
        session.setExpiresAt(LocalDateTime.now().plusDays(1));
        sessionRepository.save(session);

        // Precondition: session exists
        Assertions.assertTrue(sessionRepository.findByUserId(user.getId()).isPresent());

        // Act
        authenticationService.logout(email);

        // Assert: session removed
        Assertions.assertTrue(sessionRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void logout_noActiveSession_doesNothing() {
        String email = "logout.no.session@test.com";
        User user = persistUser(email, true, "pw", 0, false);

        // Precondition: no session
        Assertions.assertTrue(sessionRepository.findByUserId(user.getId()).isEmpty());

        // Act (should not throw)
        authenticationService.logout(email);

        // Still no session
        Assertions.assertTrue(sessionRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void logout_fails_whenEmailNotFound() {
        AuthException ex = Assertions.assertThrows(
                AuthException.class,
                () -> authenticationService.logout("missing@test.com")
        );
        Assertions.assertEquals(ErrorCode.EMAIL_NOT_FOUND.getCode(), ex.getErrorCode());
    }

    private User saveUser(String email) {
        EmailAddressVO emailAddress = new EmailAddressVO(email, false);
        User user = new User(emailAddress, null, 0, false);
        return userRepository.save(user);
    }

    private void saveUserVerification(User user) {
        LocalDateTime expiresTime = LocalDateTime.now().plusSeconds(UserApplicationConstant.VERIFICATION_EXPIRES_TIME);
        String token = UUID.randomUUID().toString();
        VerificationTokenVO verificationToken = new VerificationTokenVO(token, expiresTime);

        UserVerification verification = new UserVerification(user, verificationToken, false);
        userVerificationRepository.save(verification);
    }
}
