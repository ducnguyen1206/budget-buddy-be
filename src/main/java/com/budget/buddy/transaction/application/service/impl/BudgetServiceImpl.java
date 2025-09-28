package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.budget.BudgetDTO;
import com.budget.buddy.transaction.application.service.BudgetService;
import com.budget.buddy.transaction.domain.service.BudgetData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {
    private final BudgetData budgetData;

    @Override
    public void saveBudget(BudgetDTO budgetDTO) {
        budgetData.saveBudget(budgetDTO);
    }

    @Override
    public void updateBudget(BudgetDTO budgetDTO, Long budgetId) {
        budgetData.updateBudget(budgetDTO, budgetId);
    }

    @Override
    public void deleteBudget(Long budgetId) {
        budgetData.deleteBudget(budgetId);
    }

    @Override
    public List<BudgetDTO> getAllBudgetsForCurrentUser() {
        return budgetData.getAllBudgetsForCurrentUser();
    }

    @Override
    public BudgetDTO getBudgetById(Long budgetId) {
        return budgetData.getBudgetById(budgetId);
    }
}
