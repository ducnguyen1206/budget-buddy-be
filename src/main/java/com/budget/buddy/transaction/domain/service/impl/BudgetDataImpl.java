package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.budget.BudgetDTO;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.budget.Budget;
import com.budget.buddy.transaction.domain.model.category.Category;
import com.budget.buddy.transaction.domain.service.BudgetData;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.domain.vo.MoneyVO;
import com.budget.buddy.transaction.infrastructure.repository.BudgetRepository;
import com.budget.buddy.transaction.infrastructure.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetDataImpl implements BudgetData {
    private static final Logger logger = LogManager.getLogger(BudgetDataImpl.class);

    private final BudgetRepository budgetRepository;
    private final TransactionUtils transactionUtils;
    private final CategoryRepository categoryRepository;

    @Override
    public void saveBudget(BudgetDTO budgetDTO) {
        boolean budgetExists = budgetRepository.existsByCategoryIdAndMoney_Currency(budgetDTO.categoryId(), budgetDTO.currency());
        logger.info("Checking if budget exists for categoryId='{}': {}", budgetDTO.categoryId(), budgetExists);

        if (budgetExists) {
            throw new ConflictException(ErrorCode.BUDGET_EXISTED_FOR_CATEGORY_ID);
        }

        Long userId = transactionUtils.getCurrentUserId();

        Category category = categoryRepository.findBydId(budgetDTO.categoryId(), userId)
                .orElseThrow(() -> new ConflictException(ErrorCode.CATEGORY_NOT_FOUND));

        logger.info("Save budget for userId='{}', categoryId='{}', amount='{}', currency='{}'",
                userId, budgetDTO.categoryId(), budgetDTO.amount(), budgetDTO.currency());

        MoneyVO moneyVO = new MoneyVO(budgetDTO.amount(), Currency.valueOf(budgetDTO.currency()));

        // Logic to save the budget would go here
        Budget budget = new Budget(userId, category, moneyVO);
        budgetRepository.save(budget);
        logger.info("Budget saved successfully for categoryId='{}'", budgetDTO.categoryId());
    }

    @Override
    public void updateBudget(BudgetDTO budgetDTO, Long budgetId) {
        Long userId = transactionUtils.getCurrentUserId();

        Optional<Budget> budgetOptional = budgetRepository.findBydId(budgetId, userId);
        logger.info("Checking update request if budget exists for budgetId='{}': {}", budgetId, budgetOptional.isPresent());

        if (budgetOptional.isEmpty()) {
            throw new NotFoundException(ErrorCode.BUDGET_NOT_FOUND);
        }

        Budget budget = budgetOptional.get();

        MoneyVO currentMoneyVO = budget.getMoney();
        MoneyVO newMoneyVO = new MoneyVO(budgetDTO.amount(), Currency.valueOf(budgetDTO.currency()));

        boolean moneyUnchanged = currentMoneyVO.equals(newMoneyVO);
        logger.info("Current money: {}, New money: {}, Unchanged: {}", currentMoneyVO, newMoneyVO, moneyUnchanged);

        boolean categoryUnchanged = budget.getCategory().getId().equals(budgetDTO.categoryId());
        if (moneyUnchanged && categoryUnchanged) {
            logger.info("No changes detected for budgetId='{}'. Update operation skipped.", budgetId);
            return; // No changes, skip update
        }

        Category category = categoryRepository.findBydId(budgetDTO.categoryId(), userId)
                .orElseThrow(() -> new ConflictException(ErrorCode.CATEGORY_NOT_FOUND));
        budget.setCategory(category);
        budget.setMoney(newMoneyVO);
        budgetRepository.save(budget);
        logger.info("Budget updated successfully for budgetId='{}'", budgetId);
    }

    @Override
    public void deleteBudget(Long budgetId) {
        Long userId = transactionUtils.getCurrentUserId();

        Optional<Budget> budgetOptional = budgetRepository.findBydId(budgetId, userId);
        logger.info("Checking delete request if budget exists for budgetId='{}': {}", budgetId, budgetOptional.isPresent());

        if (budgetOptional.isEmpty()) {
            throw new NotFoundException(ErrorCode.BUDGET_NOT_FOUND);
        }

        budgetRepository.deleteById(budgetId);
        logger.info("Budget deleted successfully for budgetId='{}'", budgetId);
    }


    @Override
    public List<BudgetDTO> getAllBudgetsForCurrentUser() {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Retrieving all budgets for userId='{}'", userId);
        List<BudgetDTO> budgets = budgetRepository.findAllBudgetsForUser();
        logger.info("Retrieved {} budgets for userId='{}'", budgets.size(), userId);
        return budgets;
    }

    @Override
    public BudgetDTO getBudgetById(Long budgetId) {
        Optional<BudgetDTO> budgetDTO = budgetRepository.findBudgetDTOByIdAndUserId(budgetId);
        return budgetDTO.orElseThrow(() -> new NotFoundException(ErrorCode.BUDGET_NOT_FOUND));
    }
}
