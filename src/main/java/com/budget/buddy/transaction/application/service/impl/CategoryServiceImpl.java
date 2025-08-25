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

    /**
     * Creates a new category based on the provided category data.
     *
     * @param categoryRequest the data for the category to be created, including its name and type
     * @return the created category with its unique identifier and provided attributes
     */
    @Override
    public CategoryDTO createCategory(CategoryDTO categoryRequest) {
        return categoryData.createCategory(categoryRequest);
    }

    /**
     * Retrieves the category with the specified unique identifier.
     *
     * @param categoryId the unique identifier of the category to retrieve
     * @return the category data transfer object containing the details of the specified category
     */
    @Override
    public CategoryDTO getCategory(Long categoryId) {
        return categoryData.getCategory(categoryId);
    }

    /**
     * Retrieves a list of categories associated with the current user.
     *
     * @return a list of CategoryDTO objects representing the user's categories
     */
    @Override
    public List<CategoryDTO> getMyCategories() {
        return categoryData.getCategories();
    }

    /**
     * Deletes the category identified by the given unique identifier.
     *
     * @param categoryId the unique identifier of the category to delete
     */
    @Override
    public void deleteCategory(Long categoryId) {
        categoryData.deleteCategory(categoryId);
    }

    /**
     * Updates an existing category with the specified unique identifier using the provided updated data.
     *
     * @param categoryId the unique identifier of the category to be updated
     * @param categoryRequest the data transfer object containing updated details of the category
     * @return the updated CategoryDTO object reflecting the changes made
     */
    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest) {
        return categoryData.updateCategory(categoryId, categoryRequest);
    }
}
