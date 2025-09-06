package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.service.AccountService;
import com.budget.buddy.transaction.domain.service.AccountData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountData accountData;

    @Override
    public void creteAccount(AccountDTO accountDTO) {
        accountData.createAccount(accountDTO);
    }
}
