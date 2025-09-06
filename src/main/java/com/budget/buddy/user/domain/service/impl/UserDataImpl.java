package com.budget.buddy.user.domain.service.impl;

import com.budget.buddy.user.domain.model.Session;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.model.UserVerification;
import com.budget.buddy.user.domain.service.UserData;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;
import com.budget.buddy.user.infrastructure.repository.SessionRepository;
import com.budget.buddy.user.infrastructure.repository.UserRepository;
import com.budget.buddy.user.infrastructure.repository.UserVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDataImpl implements UserData {
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final SessionRepository sessionRepository;

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmailAddress_Value(email.toLowerCase());
    }

    @Override
    public Optional<User> findActiveUser(String email) {
        return userRepository.findByEmailAddress_ValueAndEmailAddress_ActiveAndLocked(email.toLowerCase(), true, false);
    }

    @Override
    public User saveNewUser(EmailAddressVO email) {
        User user = new User(email, null, 0, false);
        return userRepository.save(user);
    }

    @Override
    public UserVerification saveNewUserVerificationToken(User user, VerificationTokenVO token) {
        UserVerification verification = new UserVerification(user, token, false);
        return userVerificationRepository.save(verification);
    }

    @Override
    public Optional<UserVerification> findUserVerificationWithDate(String token, LocalDateTime time) {
        return userVerificationRepository.findByVerificationToken_valueAndVerificationToken_ExpiresAtAfter(token, time);
    }

    @Override
    public Optional<UserVerification> findUserVerification(String token) {
        return userVerificationRepository.findByVerificationToken_value(token);
    }

    @Override
    public UserVerification saveUserVerification(UserVerification userVerification) {
        return userVerificationRepository.save(userVerification);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUserVerification(UserVerification userVerification) {
        userVerificationRepository.delete(userVerification);
    }

    @Override
    public Optional<Session> findSessionByUserId(Long userId) {
        return sessionRepository.findByUserId(userId);
    }

    @Override
    public void deleteSession(Session session) {
        sessionRepository.delete(session);
    }

    @Override
    public void saveSession(Session session) {
        sessionRepository.save(session);
    }
}
