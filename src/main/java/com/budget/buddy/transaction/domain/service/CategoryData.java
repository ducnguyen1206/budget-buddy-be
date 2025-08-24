package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CategoryData {
    CategoryDTO createCategory(CategoryDTO categoryRequest);

    CategoryDTO getCategory(Long categoryId);

    List<CategoryDTO> getCategories();

    @Transactional
    void deleteCategory(Long categoryId);

    @Transactional
    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest);
}
