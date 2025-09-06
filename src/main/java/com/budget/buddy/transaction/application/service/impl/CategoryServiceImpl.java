package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.application.service.CategoryService;
import com.budget.buddy.transaction.domain.service.CategoryData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryData categoryData;


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

    @Override
    public void deleteCategory(Long categoryId) {
        categoryData.deleteCategory(categoryId);
    }


    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest) {
        return categoryData.updateCategory(categoryId, categoryRequest);
    }
}
