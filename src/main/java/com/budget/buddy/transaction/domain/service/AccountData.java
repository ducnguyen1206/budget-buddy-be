package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;

import java.util.List;

public interface AccountData {
    void createAccount(AccountDTO accountDTO);

    List<AccountRetrieveResponse> retrieveAccounts();

    AccountRetrieveResponse retrieveAccount(Long accountId);

    void deleteAccount(Long accountId);

    void updateAccount(Long accountId, AccountDTO accountDTO);
}
