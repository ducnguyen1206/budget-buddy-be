package com.budget.buddy.user.domain.service;

import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.model.UserVerification;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;

public interface UserData {
    User findUserByEmail(String email);

    boolean existsByEmail(String email);

    User saveNewUser(EmailAddressVO email);

    UserVerification saveNewUserVerificationToken(User user, VerificationTokenVO token);
}
