package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.application.mapper.CategoryMapper;
import com.budget.buddy.transaction.domain.model.category.Category;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.domain.vo.CategoryVO;
import com.budget.buddy.transaction.infrastructure.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryDataImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TransactionUtils transactionUtils;

    @InjectMocks
    private CategoryDataImpl categoryData;

    public CategoryDataImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCategory_NewCategory() {
        // Arrange
        Long userId = 1L;
        String categoryName = "Food";

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(categoryRepository.findByUserIdAndIdentity_Name(userId, categoryName)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(1L);
            return category;
        });
        when(categoryMapper.toDto(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            return new CategoryDTO(category.getId(), category.getIdentity().getName());
        });

        CategoryDTO categoryRequest = new CategoryDTO(null, categoryName);

        // Act
        CategoryDTO createdCategory = categoryData.createCategory(categoryRequest);

        // Assert
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertEquals(categoryName, createdCategory.name());
    }

    @Test
    void testCreateCategory_ExistingCategory() {
        // Arrange
        Long userId = 1L;
        String categoryName = "Food";
        Category existingCategory = new Category(new CategoryVO(categoryName), userId);
        existingCategory.setId(1L);

        when(transactionUtils.getCurrentUserId()).thenReturn(userId);
        when(categoryRepository.findByUserIdAndIdentity_Name(userId, categoryName)).thenReturn(Optional.of(existingCategory));
        when(categoryMapper.toDto(existingCategory)).thenReturn(new CategoryDTO(existingCategory.getId(), existingCategory.getIdentity().getName()));

        CategoryDTO categoryRequest = new CategoryDTO(null, categoryName);

        // Act
        CategoryDTO createdCategory = categoryData.createCategory(categoryRequest);

        // Assert
        verify(categoryRepository, never()).save(any(Category.class));
        assertEquals(categoryName, createdCategory.name());
        assertEquals(existingCategory.getId(), createdCategory.id());
    }
}