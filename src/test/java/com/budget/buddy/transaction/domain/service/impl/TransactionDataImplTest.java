package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.category.Category;
import com.budget.buddy.transaction.domain.model.transaction.Transaction;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.domain.vo.CategoryVO;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.CategoryRepository;
import com.budget.buddy.transaction.infrastructure.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DataJpaTest
class TransactionDataImplTest {

    @InjectMocks
    private TransactionDataImpl transactionData;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionUtils transactionUtils;

    @Test
    void testCreateTransaction_withValidIncomeTransaction_shouldSaveTransaction() {
        // Arrange
        Long userId = 1L;
        Long accountId = 10L;
        Long categoryId = 100L;

        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Salary")
                .amount(BigDecimal.valueOf(5000))
                .accountId(accountId)
                .categoryId(categoryId)
                .date(LocalDate.now())
                .categoryType(CategoryType.INCOME)
                .remarks("Monthly salary")
                .build();

        Account account = new Account();
        account.setId(accountId);

        Category category = new Category(new CategoryVO("TEST"), 1L);
        category.setId(categoryId);

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(accountRepository.findAccountByUserIdAndAccountId(userId, accountId)).thenReturn(account);
        when(categoryRepository.findBydId(categoryId, userId)).thenReturn(java.util.Optional.of(category));

        // Act
        transactionData.createTransaction(transactionDTO);

        // Assert
        verify(transactionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateTransaction_withTransferTransaction_shouldSaveBothTransactions() {
        // Arrange
        Long userId = 2L;
        Long sourceAccountId = 20L;
        Long targetAccountId = 30L;
        Long categoryId = 200L;

        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Transfer to Savings")
                .amount(BigDecimal.valueOf(1000))
                .accountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .categoryId(categoryId)
                .date(LocalDate.now())
                .categoryType(CategoryType.TRANSFER)
                .remarks("Savings transfer")
                .build();

        Account sourceAccount = new Account();
        sourceAccount.setId(sourceAccountId);

        Account targetAccount = new Account();
        targetAccount.setId(targetAccountId);

        Category category = new Category(new CategoryVO("TEST"), 1L);
        category.setId(categoryId);

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(accountRepository.findAccountByUserIdAndAccountId(userId, sourceAccountId)).thenReturn(sourceAccount);
        when(accountRepository.findAccountByUserIdAndAccountId(userId, targetAccountId)).thenReturn(targetAccount);
        when(categoryRepository.findBydId(categoryId, userId)).thenReturn(java.util.Optional.of(category));

        // Act
        transactionData.createTransaction(transactionDTO);

        // Assert
        verify(transactionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateTransaction_withNonExistingAccount_shouldThrowException() {
        // Arrange
        Long userId = 3L;
        Long accountId = 999L;
        Long categoryId = 300L;

        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Groceries")
                .amount(BigDecimal.valueOf(150))
                .accountId(accountId)
                .categoryId(categoryId)
                .date(LocalDate.now())
                .categoryType(CategoryType.EXPENSE)
                .remarks("Supermarket shopping")
                .build();

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(accountRepository.findAccountByUserIdAndAccountId(userId, accountId)).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> transactionData.createTransaction(transactionDTO));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testCreateTransaction_withInvalidCategory_shouldThrowException() {
        // Arrange
        Long userId = 4L;
        Long accountId = 40L;
        Long categoryId = 400L;

        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Electricity Bill")
                .amount(BigDecimal.valueOf(120))
                .accountId(accountId)
                .categoryId(categoryId)
                .date(LocalDate.now())
                .categoryType(CategoryType.EXPENSE)
                .remarks("Monthly electricity bill")
                .build();

        Account account = new Account();
        account.setId(accountId);

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(accountRepository.findAccountByUserIdAndAccountId(userId, accountId)).thenReturn(account);
        when(categoryRepository.findBydId(categoryId, userId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> transactionData.createTransaction(transactionDTO));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}