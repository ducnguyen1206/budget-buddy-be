package com.budget.buddy.user.application.config.security;

import com.budget.buddy.user.application.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultUserContext implements UserContext {
    private final UserService userService;

    @Override
    public Long getCurrentUserId() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findUserIdByEmail(email);
    }
}
