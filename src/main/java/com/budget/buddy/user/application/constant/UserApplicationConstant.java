package com.budget.buddy.user.application.constant;

public class UserApplicationConstant {
    private UserApplicationConstant() {
    }

    // In second
    public static final int VERIFICATION_EXPIRES_TIME = 900;
    public static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 days
}
