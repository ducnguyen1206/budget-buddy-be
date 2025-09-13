package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.application.dto.account.AccountTypeRetrieveResponse;

import java.util.List;

public interface AccountData {
    void createAccount(AccountDTO accountDTO);

    boolean isAccountCurrencyInvalid(AccountDTO accountDTO, Long accountId);

    List<AccountRetrieveResponse> retrieveAccounts();

    AccountRetrieveResponse retrieveAccount(Long accountId);

    void deleteAccount(Long accountId);

    void updateAccount(Long accountId, AccountDTO accountDTO);

    AccountTypeRetrieveResponse getAccountTypeGroups();

    void deleteAccountTypeGroups(Long groupId);

    void checkAccountExists(Long accountId);
}
