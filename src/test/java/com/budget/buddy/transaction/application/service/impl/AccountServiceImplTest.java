package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.application.dto.account.AccountTypeRetrieveResponse;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.service.AccountData;
import com.budget.buddy.transaction.domain.service.TransactionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    @Mock
    private AccountData accountData;
    @Mock
    private TransactionData transactionData;

    private AccountServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AccountServiceImpl(accountData, transactionData);
    }

    @Test
    void createAccount_valid_callsCreate() {
        AccountDTO dto = new AccountDTO(null, "Main", BigDecimal.ZERO, Currency.SGD, null, null, false);
        when(accountData.isAccountCurrencyInvalid(dto, null)).thenReturn(false);

        service.creteAccount(dto);

        verify(accountData).createAccount(dto);
    }

    @Test
    void createAccount_invalidCurrency_throwsConflict() {
        AccountDTO dto = new AccountDTO(null, "Main", BigDecimal.ZERO, Currency.SGD, null, null, false);
        when(accountData.isAccountCurrencyInvalid(dto, null)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.creteAccount(dto));
        assertEquals(ErrorCode.INVALID_CURRENCY.getCode(), ex.getErrorCode());
        verify(accountData, never()).createAccount(any());
    }

    @Test
    void retrieveAccounts_delegates() {
        when(accountData.retrieveAccounts(any())).thenReturn(List.of(new AccountRetrieveResponse("CASH", List.of())));

        List<AccountRetrieveResponse> res = service.retrieveAccounts(false);
        assertEquals(1, res.size());
        verify(accountData).retrieveAccounts(false);
    }

    @Test
    void retrieveAccount_delegates() {
        AccountRetrieveResponse resp = new AccountRetrieveResponse("BANK", List.of());
        when(accountData.retrieveAccount(10L)).thenReturn(resp);

        assertSame(resp, service.retrieveAccount(10L));
        verify(accountData).retrieveAccount(10L);
    }

    @Test
    void deleteAccount_withTransactions_deletesTransactionsThenAccount() {
        when(accountData.isTransactionExistedByAccountId(5L)).thenReturn(true);

        service.deleteAccount(5L);

        verify(transactionData).deleteTransactionByAccountId(eq(List.of(5L)));
        verify(accountData).checkAccountExists(5L);
        verify(accountData).deleteAccount(5L);
    }

    @Test
    void deleteAccount_withoutTransactions_deletesAccount() {
        when(accountData.isTransactionExistedByAccountId(6L)).thenReturn(false);

        service.deleteAccount(6L);

        verify(transactionData, never()).deleteTransactionByAccountId(any());
        verify(accountData).checkAccountExists(6L);
        verify(accountData).deleteAccount(6L);
    }

    @Test
    void updateAccount_valid_updates() {
        AccountDTO dto = new AccountDTO(1L, "A", BigDecimal.ONE, Currency.VND, null, 2L, false);
        when(accountData.isAccountCurrencyInvalid(dto, 9L)).thenReturn(false);

        service.updateAccount(9L, dto);

        verify(accountData).checkAccountExists(9L);
        verify(accountData).updateAccount(9L, dto);
    }

    @Test
    void updateAccount_invalidCurrency_throwsConflict() {
        AccountDTO dto = new AccountDTO(1L, "A", BigDecimal.ONE, Currency.VND, null, 2L, false);
        when(accountData.isAccountCurrencyInvalid(dto, 9L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.updateAccount(9L, dto));
        assertEquals(ErrorCode.INVALID_CURRENCY.getCode() , ex.getErrorCode());
        verify(accountData).checkAccountExists(9L);
        verify(accountData, never()).updateAccount(anyLong(), any());
    }

    @Test
    void retrieveAccountTypes_delegates() {
        AccountTypeRetrieveResponse expected = new AccountTypeRetrieveResponse(List.of("BANK"));
        when(accountData.getAccountTypeGroups()).thenReturn(expected);
        assertSame(expected, service.retrieveAccountTypes());
        verify(accountData).getAccountTypeGroups();
    }

    @Test
    void deleteAccountTypeGroup_withTransactions_cascades() {
        when(accountData.isTransactionExistedByGroupAccountId(3L)).thenReturn(true);
        when(accountData.getAccountIdsByGroupId(3L)).thenReturn(List.of(1L,2L));

        service.deleteAccountTypeGroup(3L);

        verify(transactionData).deleteTransactionByAccountId(eq(List.of(1L,2L)));
        verify(accountData).deleteAccountTypeGroups(3L);
    }

    @Test
    void deleteAccountTypeGroup_noTransactions_simpleDelete() {
        when(accountData.isTransactionExistedByGroupAccountId(4L)).thenReturn(false);

        service.deleteAccountTypeGroup(4L);

        verify(transactionData, never()).deleteTransactionByAccountId(any());
        verify(accountData).deleteAccountTypeGroups(4L);
    }
}
