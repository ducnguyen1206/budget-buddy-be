package com.budget.buddy.transaction.domain.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.budget.buddy.core.utils.ApplicationUtil.getUserIdFromContext;

@Component
@RequiredArgsConstructor
public class TransactionUtils {

    public Long getCurrentUserId() {
        return getUserIdFromContext();
    }
}
