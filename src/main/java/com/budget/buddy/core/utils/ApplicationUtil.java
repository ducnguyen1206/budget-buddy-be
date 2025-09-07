package com.budget.buddy.core.utils;

import org.springframework.security.core.context.SecurityContextHolder;

public class ApplicationUtil {
    private ApplicationUtil() {
    }

    public static String getEmailFromContext() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
