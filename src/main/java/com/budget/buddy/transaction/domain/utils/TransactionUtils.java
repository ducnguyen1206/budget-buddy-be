package com.budget.buddy.transaction.domain.utils;

import com.budget.buddy.user.application.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.budget.buddy.core.utils.ApplicationUtil.getEmailFromContext;

@Component
@RequiredArgsConstructor
public class TransactionUtils {
    private final UserService userService;

    public Long getCurrentUserId() {
        String email = getEmailFromContext();
        return userService.findUserIdByEmail(email);
    }
}
