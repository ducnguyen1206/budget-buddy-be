package com.budget.buddy.core.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class ApplicationUtil {
    private ApplicationUtil() {
    }

    public static Long getUserIdFromContext() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getName())
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalStateException("User not logged in"));
    }
}
