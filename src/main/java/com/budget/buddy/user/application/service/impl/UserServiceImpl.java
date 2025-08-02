package com.budget.buddy.user.application.service.impl;

import com.budget.buddy.user.application.service.UserService;
import com.budget.buddy.user.application.dto.UserDTO;
import com.budget.buddy.user.application.mapper.UserMapper;
import com.budget.buddy.user.domain.model.User;
import com.budget.buddy.user.domain.service.UserData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserData userData;
    private final UserMapper userMapper;

    @Override
    public UserDTO findUserByEmail(String email) {
        User user = userData.findUserByEmail(email);
        return userMapper.toDto(user);
    }
}
