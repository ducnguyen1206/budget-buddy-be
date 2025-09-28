package com.budget.buddy.transaction.application.service;

import com.budget.buddy.transaction.application.dto.budget.BudgetDTO;

import java.util.List;

public interface BudgetService {
    void saveBudget(BudgetDTO budgetDTO);

    void updateBudget(BudgetDTO budgetDTO, Long budgetId);

    void deleteBudget(Long budgetId);

    List<BudgetDTO> getAllBudgetsForCurrentUser();

    BudgetDTO getBudgetById(Long budgetId);
}
