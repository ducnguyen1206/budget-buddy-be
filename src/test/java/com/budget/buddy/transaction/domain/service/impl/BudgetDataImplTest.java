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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;

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

    @Test
    void getAllBudgetsForCurrentUser_withoutCurrency_callsRepositoryWithCalculatedDates() {
        Long userId = 5L;
        when(transactionUtils.getCurrentUserId()).thenReturn(userId);

        List<BudgetDTO> expected = List.of(new BudgetDTO(1L, 2L, "Cat", new BigDecimal("10"), null, null, "SGD", null));
        when(budgetRepository.findAllBudgetsForUser(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(expected);

        List<BudgetDTO> result = budgetDataImpl.getAllBudgetsForCurrentUser(null);
        assertSame(expected, result);

        ArgumentCaptor<Long> userCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(budgetRepository).findAllBudgetsForUser(userCaptor.capture(), startCaptor.capture(), endCaptor.capture());

        assertEquals(userId, userCaptor.getValue());

        LocalDate today = LocalDate.now();
        LocalDate expectedStart;
        LocalDate expectedEnd;
        if (today.getDayOfMonth() >= 7) {
            expectedStart = today.withDayOfMonth(7);
            expectedEnd = today.plusMonths(1).withDayOfMonth(7);
        } else {
            expectedStart = today.minusMonths(1).withDayOfMonth(7);
            expectedEnd = today.withDayOfMonth(7);
        }
        assertEquals(expectedStart, startCaptor.getValue());
        assertEquals(expectedEnd, endCaptor.getValue());
    }

    @Test
    void getAllBudgetsForCurrentUser_withCurrency_callsRepositoryWithCalculatedDates() {
        Long userId = 7L;
        String currency = "SGD";
        when(transactionUtils.getCurrentUserId()).thenReturn(userId);

        List<BudgetDTO> expected = List.of(new BudgetDTO(3L, 4L, "Cat2", new BigDecimal("20"), null, null, currency, null));
        when(budgetRepository.findAllBudgetsForUserAndCurrency(anyLong(), eq(currency), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(expected);

        List<BudgetDTO> result = budgetDataImpl.getAllBudgetsForCurrentUser(currency);
        assertSame(expected, result);

        ArgumentCaptor<Long> userCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> currencyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(budgetRepository).findAllBudgetsForUserAndCurrency(userCaptor.capture(), currencyCaptor.capture(), startCaptor.capture(), endCaptor.capture());

        assertEquals(userId, userCaptor.getValue());
        assertEquals(currency, currencyCaptor.getValue());

        LocalDate today = LocalDate.now();
        LocalDate expectedStart;
        LocalDate expectedEnd;
        if (today.getDayOfMonth() >= 7) {
            expectedStart = today.withDayOfMonth(7);
            expectedEnd = today.plusMonths(1).withDayOfMonth(7);
        } else {
            expectedStart = today.minusMonths(1).withDayOfMonth(7);
            expectedEnd = today.withDayOfMonth(7);
        }
        assertEquals(expectedStart, startCaptor.getValue());
        assertEquals(expectedEnd, endCaptor.getValue());
    }
}