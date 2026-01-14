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
import com.budget.buddy.transaction.infrastructure.repository.TransactionSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private TransactionSpecification transactionSpecification;

    @Test
    void testCreateTransaction_withValidIncomeTransaction_shouldSaveTransaction() {
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
        when(accountRepository.findAccountByUserIdAndAccountIdIn(eq(userId), anyList())).thenReturn(List.of(account));
        when(categoryRepository.findByIdInAndUserId(anyList(), eq(userId))).thenReturn(List.of(category));

        transactionData.createTransaction(transactionDTO);

        verify(transactionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateTransaction_withTransferTransaction_shouldSaveBothTransactions() {
        Long userId = 2L;
        Long sourceAccountId = 20L;
        Long targetAccountId = 30L;
        Long sourceCategoryId = 200L;
        Long targetCategoryId = 201L;

        TransactionDTO transactionDTO = TransactionDTO.builder()
                .name("Transfer to Savings")
                .amount(BigDecimal.valueOf(1000))
                .accountId(sourceAccountId)
                .targetAccountId(targetAccountId)
                .categoryId(sourceCategoryId)
                .targetCategoryId(targetCategoryId)
                .date(LocalDate.now())
                .categoryType(CategoryType.TRANSFER)
                .remarks("Savings transfer")
                .build();

        Account sourceAccount = new Account();
        sourceAccount.setId(sourceAccountId);

        Account targetAccount = new Account();
        targetAccount.setId(targetAccountId);

        Category sourceCategory = new Category(new CategoryVO("Transfer Out"), userId);
        sourceCategory.setId(sourceCategoryId);

        Category targetCategory = new Category(new CategoryVO("Transfer In"), userId);
        targetCategory.setId(targetCategoryId);

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(accountRepository.findAccountByUserIdAndAccountIdIn(eq(userId), anyList()))
                .thenReturn(List.of(sourceAccount, targetAccount));
        when(categoryRepository.findByIdInAndUserId(anyList(), eq(userId)))
                .thenReturn(List.of(sourceCategory, targetCategory));

        transactionData.createTransaction(transactionDTO);

        verify(transactionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateTransaction_withNonExistingAccount_shouldThrowException() {
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
        when(accountRepository.findAccountByUserIdAndAccountIdIn(eq(userId), anyList())).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> transactionData.createTransaction(transactionDTO));
        verify(transactionRepository, never()).saveAll(anyList());
    }

    @Test
    void testCreateTransaction_withInvalidCategory_shouldThrowException() {
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
        when(accountRepository.findAccountByUserIdAndAccountIdIn(eq(userId), anyList())).thenReturn(List.of(account));
        when(categoryRepository.findByIdInAndUserId(anyList(), eq(userId))).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> transactionData.createTransaction(transactionDTO));
        verify(transactionRepository, never()).saveAll(anyList());
    }
}