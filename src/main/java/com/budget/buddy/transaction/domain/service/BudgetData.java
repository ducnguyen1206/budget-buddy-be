package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.budget.BudgetDTO;

import java.util.List;

public interface BudgetData {
    void saveBudget(BudgetDTO budgetDTO);

    void updateBudget(BudgetDTO budgetDTO, Long budgetId);

    void deleteBudget(Long budgetId);

    List<BudgetDTO> getAllBudgetsForCurrentUser();

    BudgetDTO getBudgetById(Long budgetId);
}
