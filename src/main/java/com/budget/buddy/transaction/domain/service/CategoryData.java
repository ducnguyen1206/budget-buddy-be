package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CategoryData {
    CategoryDTO createCategory(CategoryDTO categoryRequest);

    CategoryDTO getCategory(Long categoryId);

    List<CategoryDTO> getCategories();

    void deleteCategory(Long categoryId);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest);

    @Transactional(readOnly = true)
    boolean isTransactionExistedByCategoryId(Long accountId);
}
