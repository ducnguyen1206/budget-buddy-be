package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.domain.service.CategoryData;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest {

    /**
     * Test class for CategoryServiceImpl, specifically the createCategory method.
     * The createCategory method is responsible for delegating category creation
     * requests to CategoryData and returning the created category details.
     */

    @Mock
    private CategoryData categoryData;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    public CategoryServiceImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCategory_SuccessfulCreation() {
        // Arrange
        CategoryDTO categoryRequest = new CategoryDTO(null, "Food");
        CategoryDTO createdCategory = new CategoryDTO(1L, "Food");

        when(categoryData.createCategory(any(CategoryDTO.class))).thenReturn(createdCategory);

        // Act
        CategoryDTO result = categoryService.createCategory(categoryRequest);

        // Assert
        verify(categoryData, times(1)).createCategory(categoryRequest);
        assertEquals(createdCategory.id(), result.id());
        assertEquals(createdCategory.name(), result.name());
    }

    @Test
    void testCreateCategory_InvalidCategoryName() {
        // Arrange
        CategoryDTO categoryRequest = new CategoryDTO(null, "");

        when(categoryData.createCategory(any(CategoryDTO.class)))
                .thenThrow(new IllegalArgumentException("Category name is required"));

        try {
            // Act
            categoryService.createCategory(categoryRequest);
        } catch (IllegalArgumentException ex) {
            // Assert
            assertEquals("Category name is required", ex.getMessage());
        }

        verify(categoryData, times(1)).createCategory(categoryRequest);
    }
}