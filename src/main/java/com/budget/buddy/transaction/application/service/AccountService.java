package com.budget.buddy.transaction.application.service;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;

import java.util.List;

public interface AccountService {
    void creteAccount(AccountDTO accountDTO);
    List<AccountRetrieveResponse> retrieveAccounts();

    AccountRetrieveResponse retrieveAccount(Long accountId, Long accountGroupId);

    void deleteAccount(Long accountId, Long accountGroupId);
}
