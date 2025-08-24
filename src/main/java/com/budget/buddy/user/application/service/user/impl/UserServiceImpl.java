package com.budget.buddy.user.application.service.user.impl;

import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.user.application.dto.UserDTO;
import com.budget.buddy.user.application.mapper.UserMapper;
import com.budget.buddy.user.application.service.user.UserService;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.service.UserData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserData userData;
    private final UserMapper userMapper;

    /**
     * Finds the user ID associated with the provided email address.
     *
     * @param email the email address of the user whose ID needs to be retrieved
     * @return the ID of the user associated with the given email address
     * @throws NotFoundException if no user is found with the specified email address
     */
    @Override
    public Long findUserIdByEmail(String email) {
        User user = userData.findActiveUser(email).orElseThrow(() -> new NotFoundException(ErrorCode.EMAIL_NOT_FOUND));
        return user.getId();
    }

    @Override
    public UserDTO findActiveUser(String email) {
        User user = userData.findActiveUser(email).orElse(null);
        return userMapper.toDto(user);
    }
}
