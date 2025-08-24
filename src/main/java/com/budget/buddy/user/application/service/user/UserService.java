package com.budget.buddy.user.application.service.user;

import com.budget.buddy.user.application.dto.UserDTO;

public interface UserService {
    Long findUserIdByEmail(String email);

    UserDTO findActiveUser(String email);
}
