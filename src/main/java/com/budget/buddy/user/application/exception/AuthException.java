package com.budget.buddy.user.application.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final UserErrorCode userErrorCode;

    public AuthException(UserErrorCode userErrorCode) {
        super(userErrorCode.getMessage());
        this.userErrorCode = userErrorCode;
    }

    public String getErrorCode() {
        return userErrorCode.getCode();
    }
}
