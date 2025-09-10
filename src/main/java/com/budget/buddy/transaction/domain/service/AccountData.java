package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccountData {
    void createAccount(AccountDTO accountDTO);

    List<AccountRetrieveResponse> retrieveAccounts();

    AccountRetrieveResponse retrieveAccount(Long accountId, Long groupId);

    void deleteAccount(Long accountId, Long accountGroupId);
}
