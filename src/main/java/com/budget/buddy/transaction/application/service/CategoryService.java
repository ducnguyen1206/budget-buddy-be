package com.budget.buddy.transaction.application.service;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryRequest);

    CategoryDTO getCategory(Long categoryId);

    List<CategoryDTO> getMyCategories();

    void deleteCategory(Long categoryId);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest);
}
