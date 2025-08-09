package com.budget.buddy.user.domain.service.impl;

import com.budget.buddy.BaseIntegrationTest;
import com.budget.buddy.user.application.constant.UserApplicationConstant;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

class UserDataImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserDataImpl userData;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationRepository userVerificationRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private User newUser(String email, boolean verified, String password, int failedAttempts, boolean locked) {
        return new User(new EmailAddressVO(email, verified), password, failedAttempts, locked);
    }

    private User persistUser(String email) {
        return userRepository.save(newUser(email, true, "pw", 0, false));
    }

    private UserVerification persistVerification(User user, String token, LocalDateTime expiresAt, boolean verified) {
        return userVerificationRepository.save(
                new UserVerification(user, new VerificationTokenVO(token, expiresAt), verified)
        );
    }

    private Session persistSession(User user, String access, String refresh, LocalDateTime exp) {
        Session s = new Session();
        s.setUser(user);
        s.setToken(access);
        s.setRefreshToken(refresh);
        s.setExpiresAt(exp);
        return sessionRepository.save(s);
    }

    private Session persistSession(User user) {
        Session s = new Session();
        s.setUser(user);
        s.setToken("access-" + UUID.randomUUID());
        s.setRefreshToken("refresh-" + UUID.randomUUID());
        s.setExpiresAt(LocalDateTime.now().plusHours(2));
        return sessionRepository.save(s);
    }

    @Test
    void shouldFindUserByEmail() {
        // Arrange
        EmailAddressVO email = new EmailAddressVO("integration@test.com", true);
        User user = User.builder().emailAddress(email).build();
        userRepository.save(user);

        // Act
        Optional<User> result = userData.findUserByEmail(email.getValue());

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertTrue(result.get().getEmailAddress().isActive());
        Assertions.assertEquals(email.getValue(), result.get().getEmailAddress().getValue());
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

    @Test
    void findUserVerificationWithDateTest() {
        // Arrange
        EmailAddressVO email = new EmailAddressVO("findUserVerificationWithDateTest@test.com", false);
        User user = new User(email, null, 0, false);
        userRepository.save(user);

        LocalDateTime expiresTime = LocalDateTime.now().plusSeconds(UserApplicationConstant.VERIFICATION_EXPIRES_TIME);
        String token = UUID.randomUUID().toString();
        VerificationTokenVO verificationToken = new VerificationTokenVO(token, expiresTime);
        UserVerification verification = new UserVerification(user, verificationToken, false);

        userVerificationRepository.save(verification);

        // Act
        Optional<UserVerification> userVerification = userData.findUserVerificationWithDate(token, LocalDateTime.now());

        // Assert
        Assertions.assertTrue(userVerification.isPresent());
    }

    @Test
    void saveUserVerificationTest() {
        // Arrange
        EmailAddressVO email = new EmailAddressVO("saveUserVerificationTest@test.com", false);
        User user = new User(email, null, 0, false);
        userRepository.save(user);

        LocalDateTime expiresTime = LocalDateTime.now().plusSeconds(UserApplicationConstant.VERIFICATION_EXPIRES_TIME);
        String token = UUID.randomUUID().toString();
        VerificationTokenVO verificationToken = new VerificationTokenVO(token, expiresTime);
        UserVerification verification = new UserVerification(user, verificationToken, false);

        // Act
        UserVerification userVerification = userData.saveUserVerification(verification);

        // Assert
        Assertions.assertNotNull(userVerification);
        Assertions.assertEquals(token, userVerification.getVerificationToken().getValue());
    }


    @Test
    void findSessionByUserId_shouldReturnSessionWhenExists() {
        // Arrange: create & persist a user
        User user = User.builder().build();
        user.setEmailAddress(new EmailAddressVO("find.session@test.com", true).activate());
        user.setPassword("pw"); // or encoder in your real flow
        user.setFailedAttempts(0);
        user.setLocked(false);
        user = userRepository.save(user);

        // Persist a session for that user
        Session s = new Session();
        s.setUser(user);
        s.setToken("access-token");
        s.setRefreshToken("refresh-token");
        s.setExpiresAt(LocalDateTime.now().plusHours(1));
        sessionRepository.save(s);

        // Act
        Optional<Session> found = userData.findSessionByUserId(user.getId());

        // Assert
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("refresh-token", found.get().getRefreshToken());
    }

    @Test
    void findSessionByUserId_shouldBeEmptyWhenNone() {
        Optional<Session> found = userData.findSessionByUserId(999_999L);
        Assertions.assertTrue(found.isEmpty());
    }

    @Test
    void findUserByEmail_shouldReturnUser_caseInsensitive() {
        User saved = persistUser("case@test.com");
        Optional<User> found = userData.findUserByEmail("CASE@TEST.COM");
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findUserByEmail_shouldBeEmptyWhenUnknown() {
        Optional<User> result = userData.findUserByEmail("notfound@test.com");
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmail_shouldReflectPresence_caseInsensitive() {
        persistUser("exists@test.com");
        Assertions.assertTrue(userData.existsByEmail("EXISTS@TEST.COM"));
        Assertions.assertFalse(userData.existsByEmail("missing@test.com"));
    }

    @Test
    void saveUser_shouldPersistAndReturnWithId() {
        User saved = userData.saveUser(newUser("save@test.com", true, "pw", 0, false));
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("save@test.com", saved.getEmailAddress().toString());
    }

    @Test
    void saveNewUserVerificationToken_shouldPersistVerificationForUser() {
        User user = persistUser("verify-token@test.com");
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(UserApplicationConstant.VERIFICATION_EXPIRES_TIME);

        UserVerification saved = userData.saveNewUserVerificationToken(user, new VerificationTokenVO(token, expiresAt));

        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(user.getId(), saved.getUser().getId());
        Assertions.assertEquals(token, saved.getVerificationToken().getValue());
    }

    @Test
    void findUserVerificationWithDate_shouldReturnWhenNotExpired() {
        User user = persistUser("uv-not-expired@test.com");
        String token = UUID.randomUUID().toString();
        persistVerification(user, token, LocalDateTime.now().plusMinutes(5), false);

        Optional<UserVerification> found = userData.findUserVerificationWithDate(token, LocalDateTime.now());
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(token, found.get().getVerificationToken().getValue());
    }

    @Test
    void findUserVerificationWithDate_shouldBeEmptyWhenExpired() {
        User user = persistUser("uv-expired@test.com");
        String token = UUID.randomUUID().toString();
        persistVerification(user, token, LocalDateTime.now().minusSeconds(1), false);

        Optional<UserVerification> found = userData.findUserVerificationWithDate(token, LocalDateTime.now());
        Assertions.assertTrue(found.isEmpty());
    }

    @Test
    void saveUserVerification_shouldUpsertAndReturnEntity() {
        User user = persistUser("uv-save@test.com");
        String token = UUID.randomUUID().toString();
        UserVerification saved = userData.saveUserVerification(
                new UserVerification(user, new VerificationTokenVO(token, LocalDateTime.now().plusMinutes(10)), false)
        );
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(token, saved.getVerificationToken().getValue());
    }

    @Test
    void deleteUserVerification_shouldRemoveEntity() {
        User user = persistUser("uv-delete@test.com");
        String token = UUID.randomUUID().toString();
        UserVerification uv = persistVerification(user, token, LocalDateTime.now().plusMinutes(10), false);

        userData.deleteUserVerification(uv);

        Assertions.assertTrue(userVerificationRepository.findById(uv.getId()).isEmpty());
    }

    @Test
    void findUserVerificationByUserId_shouldReturnVerification() {
        User user = persistUser("uv-by-user@test.com");
        String token = UUID.randomUUID().toString();
        persistVerification(user, token, LocalDateTime.now().plusMinutes(10), false);

        Optional<UserVerification> found = userData.findUserVerificationByUserId(user.getId());

        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(user.getId(), found.get().getUser().getId());
    }

    @Test
    void findSessionByUserId_shouldReturnSessionWhenExists2() {
        User user = persistUser("session@test.com");
        String access = "access-" + UUID.randomUUID();
        String refresh = "refresh-" + UUID.randomUUID();
        persistSession(user, access, refresh, LocalDateTime.now().plusDays(1));

        Optional<Session> found = userData.findSessionByUserId(user.getId());

        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(refresh, found.get().getRefreshToken());
    }

    @Test
    void findSessionByUserId_shouldBeEmptyWhenNoSession() {
        Optional<Session> found = userData.findSessionByUserId(9_999_999L);
        Assertions.assertTrue(found.isEmpty());
    }

    @Test
    void deleteSession_shouldRemoveRow() {
        User user = persistUser("delete.session@test.com");
        Session session = persistSession(user);
        Long id = session.getId();

        userData.deleteSession(session);

        Assertions.assertTrue(sessionRepository.findById(id).isEmpty());
    }

    @Test
    void saveSession_shouldPersistSession() {
        // Arrange
        User user = persistUser("save.session@test.com");
        Session s = new Session();
        s.setUser(user);
        String access = "access-" + UUID.randomUUID();
        String refresh = "refresh-" + UUID.randomUUID();
        s.setToken(access);
        s.setRefreshToken(refresh);
        s.setExpiresAt(LocalDateTime.now().plusHours(2));

        // Act
        userData.saveSession(s);

        // Assert
        Assertions.assertNotNull(s.getId(), "session id should be generated");
        Session fromDb = sessionRepository.findById(s.getId()).orElseThrow();
        Assertions.assertEquals(user.getId(), fromDb.getUser().getId());
        Assertions.assertEquals(access, fromDb.getToken());
        Assertions.assertEquals(refresh, fromDb.getRefreshToken());
    }
}
