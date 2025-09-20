package com.budget.buddy.transaction.application.service;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.application.dto.account.AccountTypeRetrieveResponse;

import java.util.List;

public interface AccountService {
    void creteAccount(AccountDTO accountDTO);
    List<AccountRetrieveResponse> retrieveAccounts();

    AccountRetrieveResponse retrieveAccount(Long accountId);

    void deleteAccount(Long accountId);

    void updateAccount(Long accountId, AccountDTO accountDTO);

    AccountTypeRetrieveResponse retrieveAccountTypes();

    void deleteAccountTypeGroup(Long groupId);
}
