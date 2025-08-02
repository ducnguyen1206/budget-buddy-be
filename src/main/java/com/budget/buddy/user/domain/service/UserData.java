package com.budget.buddy.user.domain.service;

import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.model.UserVerification;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserData {
    User findUserByEmail(String email);

    boolean existsByEmail(String email);

    User saveNewUser(EmailAddressVO email);

    UserVerification saveNewUserVerificationToken(User user, VerificationTokenVO token);

    Optional<UserVerification> findUserVerificationWithDate(String token, LocalDateTime time);

    UserVerification saveUserVerification(UserVerification userVerification);
}
