package com.budget.buddy.user.application.exception;

import lombok.Getter;

@Getter
public enum UserErrorCode {
    // Auth
    LOGIN_FAILED("AUTH_001", "Invalid email or password"),
    EMAIL_EXISTS("AUTH_002", "Email already exists"),
    TOKEN_INVALID("AUTH_003", "Invalid or expired accessToken"),
    ACCOUNT_LOCKED("AUTH_004", "AccountPayload locked. Please click on forgot password to reset"),
    USER_HAS_NOT_BEEN_VERIFIED("AUTH_005", "User hasn't been verified yet"),
    ID_TOKEN_INVALID("AUTH_006", "ID token invalid"),
    INVALID_REFRESH_TOKEN("AUTH_007", "Invalid or expired refresh token");

    private final String code;
    private final String message;

    UserErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
