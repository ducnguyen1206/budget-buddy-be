package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.budget.BudgetDTO;

import java.time.LocalDate;
import java.util.List;

public interface BudgetData {
    void saveBudget(BudgetDTO budgetDTO);

    void updateBudget(BudgetDTO budgetDTO, Long budgetId);

    void deleteBudget(Long budgetId);

    void deleteBudgetByCategoryId(Long categoryId);

    List<BudgetDTO> getAllBudgetsForCurrentUser(String currency, LocalDate startDate, LocalDate endDate);

    BudgetDTO getBudgetById(Long budgetId);
}
