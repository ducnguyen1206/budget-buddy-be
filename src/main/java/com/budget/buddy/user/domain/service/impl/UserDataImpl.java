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

@Service
@RequiredArgsConstructor
public class UserDataImpl implements UserData {
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmailAddress_Value(email.toLowerCase()).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAddress_Value(email.toLowerCase());
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
}
