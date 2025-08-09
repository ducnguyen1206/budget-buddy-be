package com.budget.buddy.user.application.service;

import com.budget.buddy.user.application.dto.UserDTO;

public interface UserService {
    UserDTO findUserByEmail(String email);
}
