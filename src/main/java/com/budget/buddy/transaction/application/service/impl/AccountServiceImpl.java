package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.application.service.AccountService;
import com.budget.buddy.transaction.domain.service.AccountData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountData accountData;

    @Override
    public void creteAccount(AccountDTO accountDTO) {
        accountData.createAccount(accountDTO);
    }

    @Override
    public List<AccountRetrieveResponse> retrieveAccounts() {
        return accountData.retrieveAccounts();
    }

    @Override
    public AccountRetrieveResponse retrieveAccount(Long accountId, Long accountGroupId) {
        return accountData.retrieveAccount(accountId, accountGroupId);
    }

    @Override
    public void deleteAccount(Long accountId, Long accountGroupId) {
        accountData.deleteAccount(accountId, accountGroupId);
    }
}
