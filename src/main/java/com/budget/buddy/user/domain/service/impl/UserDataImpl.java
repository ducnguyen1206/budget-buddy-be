package com.budget.buddy.user.domain.service.impl;

import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.model.UserVerification;
import com.budget.buddy.user.domain.service.UserData;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import com.budget.buddy.user.domain.vo.VerificationTokenVO;
import com.budget.buddy.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDataImpl implements UserData {
    private final UserRepository userRepository;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmailAddress_Value(email).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAddress_Value(email);
    }

    @Override
    public User saveUser(EmailAddressVO email) {
        User user = new User(email, null, 0, false);
        return userRepository.save(user);
    }

    @Override
    public UserVerification saveUserVerification(User user, VerificationTokenVO token) {
        return null;
    }
}
