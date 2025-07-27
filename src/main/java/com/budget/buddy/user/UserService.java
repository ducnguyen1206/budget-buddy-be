package com.budget.buddy.user;

import com.budget.buddy.user.application.dto.UserDTO;

public interface UserService {
    UserDTO findUserByEmail(String email);
}
