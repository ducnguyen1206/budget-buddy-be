package com.budget.buddy.user.application.service;

import com.budget.buddy.user.application.dto.LoginResponse;
import com.budget.buddy.user.application.dto.ResetPasswordRequest;

public interface AuthenticationService {
    void generateToken(String email);

    void verifyUser(String token);

    void resetPassword(ResetPasswordRequest request);

    LoginResponse login(String email, String password);

    LoginResponse refreshToken(String refreshToken);

    void logout(String email);
}
