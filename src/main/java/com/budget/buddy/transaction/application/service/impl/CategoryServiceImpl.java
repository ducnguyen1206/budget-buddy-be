package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.application.service.CategoryService;
import com.budget.buddy.transaction.domain.service.BudgetData;
import com.budget.buddy.transaction.domain.service.CategoryData;
import com.budget.buddy.transaction.domain.service.TransactionData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryData categoryData;
    private final TransactionData transactionData;
    private final BudgetData budgetData;

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryRequest) {
        return categoryData.createCategory(categoryRequest);
    }

    @Override
    public CategoryDTO getCategory(Long categoryId) {
        return categoryData.getCategory(categoryId);
    }


    @Override
    public List<CategoryDTO> getMyCategories() {
        return categoryData.getCategories();
    }

    @Transactional
    @Override
    public void deleteCategory(Long categoryId) {
        transactionData.deleteTransactionByCategoryId(categoryId);
        budgetData.deleteBudgetByCategoryId(categoryId);
        categoryData.deleteCategory(categoryId);
    }

    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest) {
        return categoryData.updateCategory(categoryId, categoryRequest);
    }
}
