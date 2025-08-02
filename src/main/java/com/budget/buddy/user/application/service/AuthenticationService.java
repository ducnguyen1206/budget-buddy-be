package com.budget.buddy.user.application.service;

import com.budget.buddy.user.application.dto.ResetPasswordRequest;

public interface AuthenticationService {
    void registerUser(String email);

    void verifyUser(String token);

    void resetPassword(ResetPasswordRequest request);
}
