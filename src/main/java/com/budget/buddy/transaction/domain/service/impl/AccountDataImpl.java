package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.domain.service.AccountData;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.AccountTypeGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountDataImpl implements AccountData {
    private final AccountRepository accountRepository;
    private final AccountTypeGroupRepository accountTypeGroupRepository;

    @Override
    public void createAccount(AccountDTO accountDTO) {
    }

}
