package com.budget.buddy.core.config.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final ErrorCode userErrorCode;

    public AuthException(ErrorCode userErrorCode) {
        super(userErrorCode.getMessage());
        this.userErrorCode = userErrorCode;
    }

    public String getErrorCode() {
        return userErrorCode.getCode();
    }
}
