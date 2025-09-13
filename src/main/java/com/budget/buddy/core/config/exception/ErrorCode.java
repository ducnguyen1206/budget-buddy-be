package com.budget.buddy.core.config.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Auth
    LOGIN_FAILED("AUTH_001", "Invalid email or password"),
    EMAIL_EXISTS("AUTH_002", "Email already exists"),
    TOKEN_INVALID("AUTH_003", "Invalid or expired accessToken"),
    ACCOUNT_LOCKED("AUTH_004", "AccountPayload locked. Please click on forgot password to reset"),
    USER_HAS_NOT_BEEN_VERIFIED("AUTH_005", "User hasn't been verified yet"),
    ID_TOKEN_INVALID("AUTH_006", "ID token invalid"),
    INVALID_REFRESH_TOKEN("AUTH_007", "Invalid or expired refresh token"),

    // Bad request
    REENTER_PASSWORD_NOT_THE_SAME("BAD_001", "Password and re-enter password must be the same"),
    REQUEST_ACCOUNTS_MUST_NOT_BE_EMPTY("BAD_002", "Request accounts must not be empty"),
    INVALID_REQUEST_DATA("BAD_003", "Invalid request data"),

    // Not found
    EMAIL_NOT_FOUND("NOT_FOUND_001", "Email not found"),
    ACCOUNT_TYPE_GROUP_NOT_FOUND("NOT_FOUND_002", "Account type group not found"),
    CATEGORY_NOT_FOUND("NOT_FOUND_003", "Category not found"),
    USER_NOT_FOUND("NOT_FOUND_004", "User not found"),
    ACCOUNT_NOT_FOUND("NOT_FOUND_005", "Account not found"),

    // Conflict
    FAILED_TO_UPDATE_CATEGORY("CONFLICT_001", "Failed to save/update category"),
    TRANSACTION_IS_NOT_BELONG_TO_USER("CONFLICT_002", "Transaction is not belong to user"),
    INVALID_CURRENCY("CONFLICT_003", "Invalid currency in account group"),

    // Server error
    SERVER_ERROR("SYS_001", "An unexpected error occurred"),
    CALLING_TO_EXTERNAL_SERVICE_ERROR("SYS_002", "Error occur when calling to external service %s");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
