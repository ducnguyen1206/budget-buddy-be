package com.budget.buddy.user.application.utils;

import org.springframework.security.core.context.SecurityContextHolder;

public class ApplicationUtil {
    private ApplicationUtil() {
    }

    public static String getEmailFromContext() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
