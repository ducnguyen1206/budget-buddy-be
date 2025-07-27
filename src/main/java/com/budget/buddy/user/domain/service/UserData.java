package com.budget.buddy.user.domain.service;

import com.budget.buddy.user.domain.model.User;

public interface UserData {
    User findUserByEmail(String email);
}
