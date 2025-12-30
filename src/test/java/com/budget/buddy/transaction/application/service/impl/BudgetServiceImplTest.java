package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.budget.BudgetDTO;
import com.budget.buddy.transaction.domain.service.BudgetData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class BudgetServiceImplTest {

    @Mock
    private BudgetData budgetData;

    private BudgetServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new BudgetServiceImpl(budgetData);
    }

    @Test
    void saveBudget_delegates() {
        BudgetDTO dto = new BudgetDTO(1L, 10L, "Food", new java.math.BigDecimal("100.00"), null, null, "SGD", null);
        service.saveBudget(dto);
        verify(budgetData).saveBudget(dto);
    }

    @Test
    void updateBudget_delegates() {
        BudgetDTO dto = new BudgetDTO(1L, 10L, "Food", new java.math.BigDecimal("200.00"), null, null, "SGD", null);
        service.updateBudget(dto, 9L);
        verify(budgetData).updateBudget(dto, 9L);
    }

    @Test
    void deleteBudget_delegates() {
        service.deleteBudget(5L);
        verify(budgetData).deleteBudget(5L);
    }

    @Test
    void getAllBudgetsForCurrentUser_withCurrency_delegates() {
        List<BudgetDTO> expected = List.of(new BudgetDTO(1L, 2L, "Cat", new java.math.BigDecimal("10"), null, null, "SGD", null));
        when(budgetData.getAllBudgetsForCurrentUser("SGD")).thenReturn(expected);
        assertSame(expected, service.getAllBudgetsForCurrentUser("SGD"));
        verify(budgetData).getAllBudgetsForCurrentUser("SGD");
    }

    @Test
    void getAllBudgetsForCurrentUser_withoutCurrency_delegates() {
        List<BudgetDTO> expected = List.of(new BudgetDTO(1L, 2L, "Cat", new java.math.BigDecimal("10"), null, null, "SGD", null));
        when(budgetData.getAllBudgetsForCurrentUser(null)).thenReturn(expected);
        assertSame(expected, service.getAllBudgetsForCurrentUser(null));
        verify(budgetData).getAllBudgetsForCurrentUser(null);
    }

    @Test
    void getBudgetById_delegates() {
        BudgetDTO expected = new BudgetDTO(2L, 3L, "Cat", new java.math.BigDecimal("10"), null, null, "SGD", null);
        when(budgetData.getBudgetById(2L)).thenReturn(expected);
        assertSame(expected, service.getBudgetById(2L));
        verify(budgetData).getBudgetById(2L);
    }
}
