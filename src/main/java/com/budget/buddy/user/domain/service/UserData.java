package com.budget.buddy.user.domain.service;

import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.model.UserVerification;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserData {
    Optional<User> findUserByEmail(String email);

    Optional<User> findActiveUser(String email);

    User saveNewUser(EmailAddressVO email);

    void saveNewUserVerificationToken(User user, VerificationTokenVO token);

    Optional<UserVerification> findUserVerificationWithDate(String token, LocalDateTime time);

    Optional<UserVerification> findUserVerification(String token);

    void saveUserVerification(UserVerification userVerification);

    void saveUser(User user);

    void deleteUserVerification(UserVerification userVerification);

    Optional<User> findByUserId(Long userId);
}
