package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;

public interface AccountData {
    void createAccount(AccountDTO accountDTO);
}
