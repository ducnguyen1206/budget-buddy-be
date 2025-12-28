package com.budget.buddy.user.application.service.auth;

import com.budget.buddy.user.application.dto.GoogleTokenResponse;
import com.budget.buddy.user.application.dto.LoginResponse;

public interface GoogleAuthService {
    LoginResponse login(String authCode);

    GoogleTokenResponse exchangeCodeForToken(String authCode);
}
