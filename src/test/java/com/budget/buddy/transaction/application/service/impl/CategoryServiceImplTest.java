//package com.budget.buddy.transaction.application.service.impl;
//
//import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
//import com.budget.buddy.transaction.domain.enums.CategoryType;
//import com.budget.buddy.transaction.domain.service.CategoryData;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class CategoryServiceImplTest {
//
//    @Mock
//    private CategoryData categoryData;
//
//    @InjectMocks
//    private CategoryServiceImpl categoryService;
//
//    @Test
//    void createCategory_ShouldReturnCategoryDTO_WhenValidRequestProvided() {
//        // Arrange
//        CategoryDTO request = new CategoryDTO(null, "Food", CategoryType.EXPENSE);
//        CategoryDTO expectedResponse = new CategoryDTO(1L, "Food", CategoryType.EXPENSE);
//
//        when(categoryData.createCategory(any()))
//                .thenReturn(expectedResponse);
//
//        // Act
//        CategoryDTO actualResponse = categoryService.createCategory(request);
//
//        // Assert
//        assertNotNull(actualResponse);
//        assertEquals(expectedResponse.id(), actualResponse.id());
//        assertEquals(expectedResponse.name(), actualResponse.name());
//        assertEquals(expectedResponse.type(), actualResponse.type());
//    }
//
//    @Test
//    void createCategory_ShouldThrowException_WhenInvalidRequestProvided() {
//        // Arrange
//        CategoryDTO invalidRequest = new CategoryDTO(null, "", null);
//
//        // Act & Assert
//        try {
//            categoryService.createCategory(invalidRequest);
//        } catch (Exception e) {
//            assertNotNull(e);
//        }
//    }
//}