package com.budget.buddy.user.domain.service.impl;

import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.model.UserVerification;
import com.budget.buddy.user.domain.service.UserData;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;
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
    public void saveNewUserVerificationToken(User user, VerificationTokenVO token) {
        UserVerification verification = new UserVerification(user, token, false);
        userVerificationRepository.save(verification);
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
    public void saveUserVerification(UserVerification userVerification) {
        userVerificationRepository.save(userVerification);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void deleteUserVerification(UserVerification userVerification) {
        userVerificationRepository.delete(userVerification);
    }

    @Override
    public Optional<User> findByUserId(Long userId) {
        return userRepository.findById(userId);
    }
}
