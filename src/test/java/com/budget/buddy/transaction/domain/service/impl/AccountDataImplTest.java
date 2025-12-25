package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.account.AccountTypeGroup;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.AccountTypeGroupRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class AccountDataImplTest {

    @InjectMocks
    private AccountDataImpl accountDataImpl;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountTypeGroupRepository accountTypeGroupRepository;

    @Mock
    private TransactionUtils transactionUtils;

    public AccountDataImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAccount_createsNewAccount() {
        // Arrange
        Long userId = 1L;
        String accountTypeName = "SAVINGS";
        String accountName = "My Savings Account";
        Currency currency = Currency.SGD;

        AccountTypeGroup accountTypeGroup = new AccountTypeGroup(userId, accountTypeName, new ArrayList<>());
        accountTypeGroup.setUserId(userId);
        accountTypeGroup.setName(accountTypeName);
        accountTypeGroup.setAccounts(new ArrayList<>());

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(accountTypeGroupRepository.findByNameAndUserId(accountTypeName, userId))
                .thenReturn(Optional.of(accountTypeGroup));

        AccountDTO accountDTO = new AccountDTO(null, accountName, null, currency, accountTypeName, null);

        // Act
        assertDoesNotThrow(() -> accountDataImpl.createAccount(accountDTO));

        // Assert
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccount_doesNotCreateDuplicateAccount() {
        // Arrange
        Long userId = 1L;
        String accountTypeName = "CHECKING";
        String accountName = "My Checking Account";
        Currency currency = Currency.SGD;

        Account existingAccount = new Account();
        existingAccount.setName(accountName);

        AccountTypeGroup accountTypeGroup = new AccountTypeGroup(userId, accountTypeName, new ArrayList<>());
        accountTypeGroup.setUserId(userId);
        accountTypeGroup.setName(accountTypeName);
        accountTypeGroup.setAccounts(new ArrayList<>() {{
            add(existingAccount);
        }});

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(accountTypeGroupRepository.findByNameAndUserId(accountTypeName, userId))
                .thenReturn(Optional.of(accountTypeGroup));

        AccountDTO accountDTO = new AccountDTO(null, accountName, null, currency, accountTypeName, null);

        // Act
        accountDataImpl.createAccount(accountDTO);

        // Assert
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_createsAccountWithNewGroup() {
        // Arrange
        Long userId = 2L;
        String accountTypeName = "INVESTMENTS";
        String accountName = "Crypto Portfolio";
        Currency currency = Currency.SGD;

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(accountTypeGroupRepository.findByNameAndUserId(accountTypeName, userId))
                .thenReturn(Optional.empty());

        AccountTypeGroup newGroup = new AccountTypeGroup(userId, accountTypeName, new ArrayList<>());
        newGroup.setUserId(userId);
        newGroup.setName(accountTypeName);
        newGroup.setAccounts(new ArrayList<>());

        when(accountTypeGroupRepository.save(any(AccountTypeGroup.class))).thenReturn(newGroup);

        AccountDTO accountDTO = new AccountDTO(null, accountName, null, currency, accountTypeName, null);

        // Act
        assertDoesNotThrow(() -> accountDataImpl.createAccount(accountDTO));

        // Assert
        verify(accountTypeGroupRepository, times(1)).save(any(AccountTypeGroup.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}