package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.budget.BudgetDTO;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.budget.Budget;
import com.budget.buddy.transaction.domain.model.category.Category;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.domain.vo.CategoryVO;
import com.budget.buddy.transaction.domain.vo.MoneyVO;
import com.budget.buddy.transaction.infrastructure.repository.BudgetRepository;
import com.budget.buddy.transaction.infrastructure.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class BudgetDataImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionUtils transactionUtils;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BudgetDataImpl budgetDataImpl;

    private static final Long USER_ID = 1L;

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
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));

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
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

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
        if (today.getDayOfMonth() >= 5) {
            expectedStart = today.withDayOfMonth(5);
            expectedEnd = today.plusMonths(1).withDayOfMonth(5);
        } else {
            expectedStart = today.minusMonths(1).withDayOfMonth(5);
            expectedEnd = today.withDayOfMonth(5);
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
        if (today.getDayOfMonth() >= 5) {
            expectedStart = today.withDayOfMonth(5);
            expectedEnd = today.plusMonths(1).withDayOfMonth(5);
        } else {
            expectedStart = today.minusMonths(1).withDayOfMonth(5);
            expectedEnd = today.withDayOfMonth(5);
        }
        assertEquals(expectedStart, startCaptor.getValue());
        assertEquals(expectedEnd, endCaptor.getValue());
    }

    @Test
    void updateBudget_shouldThrow_whenBudgetNotFound() {
        Long budgetId = 99L;
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(budgetRepository.findByIdAndUserId(budgetId, USER_ID)).thenReturn(Optional.empty());

        BudgetDTO dto = new BudgetDTO(null, 2L, null, new BigDecimal("100"), null, null, "SGD", null);

        assertThrows(NotFoundException.class, () -> budgetDataImpl.updateBudget(dto, budgetId));
        verify(budgetRepository, never()).save(any());
    }

    @Test
    void updateBudget_shouldSkip_whenNoChanges() {
        Long budgetId = 5L;
        Long categoryId = 2L;
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        
        Category category = new Category(new CategoryVO("TEST"), USER_ID);
        category.setId(categoryId);
        Budget budget = new Budget(USER_ID, category, new MoneyVO(new BigDecimal("100.00"), Currency.SGD));
        budget.setId(budgetId);
        
        when(budgetRepository.findByIdAndUserId(budgetId, USER_ID)).thenReturn(Optional.of(budget));

        BudgetDTO dto = new BudgetDTO(null, categoryId, null, new BigDecimal("100.00"), null, null, "SGD", null);

        budgetDataImpl.updateBudget(dto, budgetId);

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void updateBudget_shouldUpdate_whenChangesExist() {
        Long budgetId = 5L;
        Long categoryId = 2L;
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        
        Category category = new Category(new CategoryVO("TEST"), USER_ID);
        category.setId(categoryId);
        Budget budget = new Budget(USER_ID, category, new MoneyVO(new BigDecimal("100.00"), Currency.SGD));
        budget.setId(budgetId);
        
        when(budgetRepository.findByIdAndUserId(budgetId, USER_ID)).thenReturn(Optional.of(budget));
        when(categoryRepository.findByIdAndUserId(categoryId, USER_ID)).thenReturn(Optional.of(category));

        BudgetDTO dto = new BudgetDTO(null, categoryId, null, new BigDecimal("200.00"), null, null, "SGD", null);

        budgetDataImpl.updateBudget(dto, budgetId);

        verify(budgetRepository).save(budget);
        assertEquals(new BigDecimal("200.00"), budget.getMoney().getAmount());
    }

    @Test
    void deleteBudget_shouldThrow_whenBudgetNotFound() {
        Long budgetId = 99L;
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(budgetRepository.findByIdAndUserId(budgetId, USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> budgetDataImpl.deleteBudget(budgetId));
        verify(budgetRepository, never()).deleteById(any());
    }

    @Test
    void deleteBudget_shouldDelete_whenBudgetExists() {
        Long budgetId = 5L;
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        
        Category category = new Category(new CategoryVO("TEST"), USER_ID);
        Budget budget = new Budget(USER_ID, category, new MoneyVO(new BigDecimal("100.00"), Currency.SGD));
        budget.setId(budgetId);
        
        when(budgetRepository.findByIdAndUserId(budgetId, USER_ID)).thenReturn(Optional.of(budget));

        budgetDataImpl.deleteBudget(budgetId);

        verify(budgetRepository).deleteById(budgetId);
    }

    @Test
    void deleteBudgetByCategoryId_shouldDoNothing_whenNoBudgetsExist() {
        Long categoryId = 2L;
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(budgetRepository.findAllByUserIdAndCategoryId(USER_ID, categoryId)).thenReturn(Collections.emptyList());

        budgetDataImpl.deleteBudgetByCategoryId(categoryId);

        verify(budgetRepository, never()).deleteAll(anyList());
    }

    @Test
    void deleteBudgetByCategoryId_shouldDeleteAll_whenBudgetsExist() {
        Long categoryId = 2L;
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        
        Category category = new Category(new CategoryVO("TEST"), USER_ID);
        Budget budget = new Budget(USER_ID, category, new MoneyVO(new BigDecimal("100.00"), Currency.SGD));
        List<Budget> budgets = List.of(budget);
        
        when(budgetRepository.findAllByUserIdAndCategoryId(USER_ID, categoryId)).thenReturn(budgets);

        budgetDataImpl.deleteBudgetByCategoryId(categoryId);

        verify(budgetRepository).deleteAll(budgets);
    }

    @Test
    void getBudgetById_shouldThrow_whenNotFound() {
        Long budgetId = 99L;
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(budgetRepository.findBudgetDTOByIdAndUserId(budgetId, USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> budgetDataImpl.getBudgetById(budgetId));
    }

    @Test
    void getBudgetById_shouldReturn_whenFound() {
        Long budgetId = 5L;
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        
        BudgetDTO expected = new BudgetDTO(budgetId, 2L, "Cat", new BigDecimal("100"), null, null, "SGD", null);
        when(budgetRepository.findBudgetDTOByIdAndUserId(budgetId, USER_ID)).thenReturn(Optional.of(expected));

        BudgetDTO result = budgetDataImpl.getBudgetById(budgetId);

        assertSame(expected, result);
    }
}