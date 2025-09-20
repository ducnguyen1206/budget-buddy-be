package com.budget.buddy.transaction.application.config.hibernate;

import com.budget.buddy.user.application.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContext {

    private final UserService userService;
    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    public void setUser(String email) {
        Long userId = userService.findUserIdByEmail(email);
        CURRENT.set(userId);
    }

    public Long getUser() {
        return CURRENT.get();
    }

    public void clear() {
        CURRENT.remove();
    }
}
