package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.application.dto.account.AccountTypeRetrieveResponse;
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
        if (accountData.isAccountCurrencyInvalid(accountDTO, null)) {
            throw new ConflictException(ErrorCode.INVALID_CURRENCY);
        }

        accountData.createAccount(accountDTO);
    }

    @Override
    public List<AccountRetrieveResponse> retrieveAccounts() {
        return accountData.retrieveAccounts();
    }

    @Override
    public AccountRetrieveResponse retrieveAccount(Long accountId) {
        return accountData.retrieveAccount(accountId);
    }

    @Override
    public void deleteAccount(Long accountId) {
        if (accountData.isTransactionExistedByAccountId(accountId)) {
            throw new ConflictException(ErrorCode.TRANSACTION_EXISTED_FOR_ACCOUNT_ID);
        }

        accountData.checkAccountExists(accountId);
        accountData.deleteAccount(accountId);
    }

    @Override
    public void updateAccount(Long accountId, AccountDTO accountDTO) {
        accountData.checkAccountExists(accountId);

        if (accountData.isAccountCurrencyInvalid(accountDTO, accountId)) {
            throw new ConflictException(ErrorCode.INVALID_CURRENCY);
        }

        accountData.updateAccount(accountId, accountDTO);
    }

    @Override
    public AccountTypeRetrieveResponse retrieveAccountTypes() {
        return accountData.getAccountTypeGroups();
    }

    @Override
    public void deleteAccountTypeGroup(Long groupId) {
        if (accountData.isTransactionExistedByGroupAccountId(groupId)) {
            throw new ConflictException(ErrorCode.TRANSACTION_EXISTED_FOR_ACCOUNT_ID);
        }

        accountData.deleteAccountTypeGroups(groupId);
    }
}
