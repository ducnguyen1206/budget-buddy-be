package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.domain.enums.CategoryType;

import java.util.List;

public interface CategoryData {
    CategoryDTO createCategory(CategoryDTO categoryRequest);

    CategoryDTO getCategory(Long categoryId);

    List<CategoryDTO> getCategories(CategoryType type);

    void deleteCategory(Long categoryId);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest);

    boolean isTransactionExistedByCategoryId(Long accountId);
}
