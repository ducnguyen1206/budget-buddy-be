package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.service.AccountData;
import com.budget.buddy.transaction.domain.service.TransactionData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    /**
     * Test class for TransactionServiceImpl, focused on verifying the behavior
     * of the `createTransaction` method under various scenarios.
     */

    private final TransactionData transactionData = mock(TransactionData.class);
    private final AccountData accountData = mock(AccountData.class);
    private final TransactionServiceImpl transactionServiceImpl = new TransactionServiceImpl(transactionData, accountData);

    @Test
    void createTransaction_shouldThrowBadRequestException_whenTargetAccountIdIsNullForTransfer() {
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Transfer Test")
                .amount(BigDecimal.valueOf(100))
                .accountId(1L)
                .date(LocalDate.now())
                .categoryId(2001L)
                .categoryType(CategoryType.TRANSFER)
                .targetAccountId(null)
                .build();

        assertThrows(BadRequestException.class, () -> transactionServiceImpl.createTransaction(transactionDTO));
        verify(accountData, times(1)).checkAccountExists(1L);
        verifyNoInteractions(transactionData);
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenSourceAndTargetAccountIdsAreEqualForTransfer() {
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Self Transfer")
                .amount(BigDecimal.valueOf(50))
                .accountId(1L)
                .targetAccountId(1L)
                .date(LocalDate.now())
                .categoryId(3001L)
                .categoryType(CategoryType.TRANSFER)
                .build();

        assertThrows(BadRequestException.class, () -> transactionServiceImpl.createTransaction(transactionDTO));
        verify(accountData, times(1)).checkAccountExists(1L);
        verifyNoInteractions(transactionData);
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenSourceOrTargetAccountDoesNotExist() {
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Invalid Account Transfer")
                .amount(BigDecimal.valueOf(70))
                .accountId(1L)
                .targetAccountId(2L)
                .date(LocalDate.now())
                .categoryId(4001L)
                .categoryType(CategoryType.TRANSFER)
                .build();

        when(accountData.retrieveAccountByIdList(List.of(1L, 2L))).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> transactionServiceImpl.createTransaction(transactionDTO));
        verify(accountData, times(1)).checkAccountExists(1L);
        verify(accountData, times(1)).retrieveAccountByIdList(List.of(1L, 2L));
        verifyNoInteractions(transactionData);
    }

    @Test
    void createTransaction_shouldThrowConflictException_whenCurrencyMismatchForTransfer() {
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Currency Mismatch Transfer")
                .amount(BigDecimal.valueOf(150))
                .accountId(1L)
                .targetAccountId(2L)
                .date(LocalDate.now())
                .categoryId(5001L)
                .categoryType(CategoryType.TRANSFER)
                .build();

        when(accountData.retrieveAccountByIdList(List.of(1L, 2L))).thenReturn(List.of(
                new AccountRetrieveResponse("1", List.of(
                        new AccountDTO(1L, "Account1", BigDecimal.valueOf(1000), Currency.SGD, "", 1L),
                        new AccountDTO(2L, "Account2", BigDecimal.valueOf(500), Currency.VND, "1", 1L)
                ))
        ));

        assertThrows(ConflictException.class, () -> transactionServiceImpl.createTransaction(transactionDTO));
        verify(accountData, times(1)).checkAccountExists(1L);
        verify(accountData, times(1)).retrieveAccountByIdList(List.of(1L, 2L));
        verifyNoInteractions(transactionData);
    }

    @Test
    void createTransaction_shouldThrowConflictException_whenInsufficientBalance() {
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Insufficient Balance Transfer")
                .amount(BigDecimal.valueOf(200))
                .accountId(1L)
                .targetAccountId(2L)
                .date(LocalDate.now())
                .categoryId(6001L)
                .categoryType(CategoryType.TRANSFER)
                .build();

        when(accountData.retrieveAccountByIdList(List.of(1L, 2L))).thenReturn(List.of(
                new AccountRetrieveResponse("1", List.of(
                        new AccountDTO(1L, "Account1", BigDecimal.valueOf(150), Currency.SGD, "1", 1L),
                        new AccountDTO(2L, "Account2", BigDecimal.valueOf(500), Currency.SGD, "1", 1L)
                ))
        ));

        assertThrows(ConflictException.class, () -> transactionServiceImpl.createTransaction(transactionDTO));
        verify(accountData, times(1)).checkAccountExists(1L);
        verify(accountData, times(1)).retrieveAccountByIdList(List.of(1L, 2L));
        verifyNoInteractions(transactionData);
    }

    @Test
    void createTransaction_shouldSucceed_whenAllValidationsPass() {
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Valid Transfer")
                .amount(BigDecimal.valueOf(100))
                .accountId(1L)
                .targetAccountId(2L)
                .date(LocalDate.now())
                .categoryId(7001L)
                .categoryType(CategoryType.TRANSFER)
                .build();

        when(accountData.retrieveAccountByIdList(List.of(1L, 2L))).thenReturn(List.of(
                new AccountRetrieveResponse("1", List.of(
                        new AccountDTO(1L, "Account1", BigDecimal.valueOf(150), Currency.SGD, "1", 1L),
                        new AccountDTO(2L, "Account2", BigDecimal.valueOf(500), Currency.SGD, "1", 1L)
                ))
        ));

        transactionServiceImpl.createTransaction(transactionDTO);

        verify(accountData, times(1)).checkAccountExists(1L);
        verify(accountData, times(1)).retrieveAccountByIdList(List.of(1L, 2L));
        verify(transactionData, times(1)).createTransaction(transactionDTO);
    }
}