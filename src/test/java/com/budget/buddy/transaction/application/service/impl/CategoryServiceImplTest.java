package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.domain.service.BudgetData;
import com.budget.buddy.transaction.domain.service.CategoryData;
import com.budget.buddy.transaction.domain.service.TransactionData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryData categoryData;

    @Mock
    private TransactionData transactionData;

    @Mock
    private BudgetData budgetData;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void testCreateCategory_SuccessfulCreation() {
        CategoryDTO categoryRequest = new CategoryDTO(null, "Food");
        CategoryDTO createdCategory = new CategoryDTO(1L, "Food");

        when(categoryData.createCategory(any(CategoryDTO.class))).thenReturn(createdCategory);

        CategoryDTO result = categoryService.createCategory(categoryRequest);

        verify(categoryData, times(1)).createCategory(categoryRequest);
        assertEquals(createdCategory.id(), result.id());
        assertEquals(createdCategory.name(), result.name());
    }

    @Test
    void testGetCategory_delegatesToData() {
        Long categoryId = 5L;
        CategoryDTO expected = new CategoryDTO(categoryId, "Food");
        when(categoryData.getCategory(categoryId)).thenReturn(expected);

        CategoryDTO result = categoryService.getCategory(categoryId);

        assertSame(expected, result);
        verify(categoryData, times(1)).getCategory(categoryId);
    }

    @Test
    void testGetMyCategories_delegatesToData() {
        List<CategoryDTO> expected = List.of(
                new CategoryDTO(1L, "Food"),
                new CategoryDTO(2L, "Transport")
        );
        when(categoryData.getCategories()).thenReturn(expected);

        List<CategoryDTO> result = categoryService.getMyCategories();

        assertSame(expected, result);
        verify(categoryData, times(1)).getCategories();
    }

    @Test
    void testDeleteCategory_deletesTransactionsBudgetsAndCategory() {
        Long categoryId = 5L;

        categoryService.deleteCategory(categoryId);

        verify(transactionData, times(1)).deleteTransactionByCategoryId(categoryId);
        verify(budgetData, times(1)).deleteBudgetByCategoryId(categoryId);
        verify(categoryData, times(1)).deleteCategory(categoryId);
    }

    @Test
    void testUpdateCategory_delegatesToData() {
        Long categoryId = 5L;
        CategoryDTO request = new CategoryDTO(null, "Updated Food");
        CategoryDTO expected = new CategoryDTO(categoryId, "Updated Food");
        when(categoryData.updateCategory(categoryId, request)).thenReturn(expected);

        CategoryDTO result = categoryService.updateCategory(categoryId, request);

        assertSame(expected, result);
        verify(categoryData, times(1)).updateCategory(categoryId, request);
    }
}