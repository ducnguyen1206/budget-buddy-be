package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.transaction.application.dto.budget.BudgetDTO;
import com.budget.buddy.transaction.domain.model.budget.Budget;
import com.budget.buddy.transaction.domain.model.category.Category;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.domain.vo.CategoryVO;
import com.budget.buddy.transaction.infrastructure.repository.BudgetRepository;
import com.budget.buddy.transaction.infrastructure.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BudgetDataImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionUtils transactionUtils;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BudgetDataImpl budgetDataImpl;

    BudgetDataImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveBudget_whenBudgetDoesNotExist_shouldSaveBudget() {
        // Arrange
        Long userId = 1L;
        Long categoryId = 2L;
        String currency = "SGD";
        BigDecimal amount = new BigDecimal("50.0");

        BudgetDTO budgetDTO = new BudgetDTO(null, categoryId, null, amount, null, null, currency, null);

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(budgetRepository.existsByCategoryIdAndMoney_CurrencyAndUserId(categoryId, currency, userId))
                .thenReturn(false);


        Category category = new Category(new CategoryVO("TEST"), 1L);
        category.setId(categoryId);
        when(categoryRepository.findBydId(categoryId, userId)).thenReturn(Optional.of(category));

        // Act
        budgetDataImpl.saveBudget(budgetDTO);

        // Assert
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void saveBudget_whenBudgetExists_shouldNotSaveBudget() {
        // Arrange
        Long userId = 1L;
        Long categoryId = 2L;
        String currency = "SGD";
        BigDecimal amount = new BigDecimal("50.0");

        BudgetDTO budgetDTO = new BudgetDTO(null, categoryId, null, amount, null, null, currency, null);

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(budgetRepository.existsByCategoryIdAndMoney_CurrencyAndUserId(categoryId, currency, userId))
                .thenReturn(true);

        // Act
        budgetDataImpl.saveBudget(budgetDTO);

        // Assert
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void saveBudget_whenCategoryNotFound_shouldThrowConflictException() {
        // Arrange
        Long userId = 1L;
        Long categoryId = 2L;
        String currency = "SGD";
        BigDecimal amount = new BigDecimal("50.0");

        BudgetDTO budgetDTO = new BudgetDTO(null, categoryId, null, amount, null, null, currency, null);

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(budgetRepository.existsByCategoryIdAndMoney_CurrencyAndUserId(categoryId, currency, userId))
                .thenReturn(false);
        when(categoryRepository.findBydId(categoryId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConflictException.class, () -> budgetDataImpl.saveBudget(budgetDTO));
        verify(budgetRepository, never()).save(any(Budget.class));
    }
}