package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.core.config.utils.JwtUtil;
import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.application.mapper.CategoryMapper;
import com.budget.buddy.transaction.domain.model.category.Category;
import com.budget.buddy.transaction.domain.service.CategoryData;
import com.budget.buddy.transaction.domain.vo.CategoryVO;
import com.budget.buddy.transaction.infrastructure.repository.CategoryRepository;
import com.budget.buddy.user.application.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the {@link CategoryData} interface for managing user categories.
 * Provides services for creating, retrieving, updating, and deleting categories.
 * Utilizes a repository for database operations, a mapper for converting between entity and DTO,
 * and a JWT utility for extracting authenticated user information.
 */
@Service
@RequiredArgsConstructor
public class CategoryDataImpl implements CategoryData {
    private static final Logger logger = LogManager.getLogger(CategoryDataImpl.class);
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final JwtUtil jwtUtil;

    /**
     * Creates a new category for the currently authenticated user.
     *
     * @param categoryRequest the data transfer object containing the name and type of the category to be created
     * @return the data transfer object representing the created category, including its unique identifier
     */
    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryRequest) {
        String email = jwtUtil.getEmailFromToken();

        logger.info("Creating category: name='{}', type='{}' for user email='{}'",
                categoryRequest.name(), categoryRequest.type(), email);
        Long userId = userService.findUserIdByEmail(email);

        var existingOpt = categoryRepository.findByUserIdAndIdentity_NameAndIdentity_Type(userId, categoryRequest.name(), categoryRequest.type());
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            logger.info("Category already exists with id={} for userId={}, skipping creation", existing.getId(), userId);
            return categoryMapper.toDto(existing);
        }

        CategoryVO categoryIdentity = new CategoryVO(categoryRequest.name(), categoryRequest.type());
        Category category = new Category(categoryIdentity, userId);


        category = categoryRepository.save(category);
        logger.info("Category created with id={} for userId={}", category.getId(), userId);

        return categoryMapper.toDto(category);
    }

    /**
     * Retrieves a specific category belonging to the currently authenticated user.
     *
     * @param categoryId the unique identifier of the category to be retrieved
     * @return a {@code CategoryDTO} containing the details of the requested category
     * @throws NotFoundException if the category does not exist or does not belong to the user
     */
    @Override
    public CategoryDTO getCategory(Long categoryId) {
        String email = jwtUtil.getEmailFromToken();
        Long userId = userService.findUserIdByEmail(email);

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        logger.info("Retrieved category id={} for userId={}", categoryId, userId);
        return categoryMapper.toDto(category);
    }

    /**
     * Retrieves a list of categories associated with the currently authenticated user.
     *
     * @return a list of CategoryDTO objects representing the user's categories
     */
    @Override
    public List<CategoryDTO> getCategories() {
        String email = jwtUtil.getEmailFromToken();
        Long userId = userService.findUserIdByEmail(email);

        List<Category> categories = categoryRepository.findAllByUserId(userId);
        logger.info("Retrieved {} categories for userId={}", categories.size(), userId);

        return categories.stream().map(categoryMapper::toDto).toList();
    }

    /**
     * Deletes a category for the currently authenticated user.
     * If the category with the specified ID does not belong to the user or does not exist,
     * no action is performed and a warning is logged.
     *
     * @param categoryId the unique identifier of the category to delete
     */
    @Transactional
    @Override
    public void deleteCategory(Long categoryId) {
        String email = jwtUtil.getEmailFromToken();
        Long userId = userService.findUserIdByEmail(email);

        long deleted = categoryRepository.deleteByIdAndUserId(categoryId, userId);
        if (deleted == 0) {
            logger.warn("Category id={} for userId={} not found", categoryId, userId);
        }

        logger.info("Deleted category id={} for userId={}", categoryId, userId);
    }

    /**
     * Updates an existing category for the currently authenticated user.
     *
     * @param categoryId       the unique identifier of the category to be updated
     * @param categoryRequest  the data transfer object containing the new name and type of the category
     * @return the data transfer object representing the updated category, including its unique identifier
     * @throws NotFoundException if the category with the provided ID is not found for the authenticated user
     */
    @Transactional
    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest) {
        String email = jwtUtil.getEmailFromToken();
        Long userId = userService.findUserIdByEmail(email);

        var existingOpt = categoryRepository.findByUserIdAndIdentity_NameAndIdentity_Type(userId, categoryRequest.name(), categoryRequest.type());
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            logger.info("Category already exists with id={} for userId={}, skipping update", existing.getId(), userId);
            return categoryMapper.toDto(existing);
        }

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        logger.info("Updating category id={} for userId={}", categoryId, userId);

        category.setIdentity(new CategoryVO(categoryRequest.name(), categoryRequest.type()));
        category = categoryRepository.save(category);
        logger.info("Updated category id={} for userId={}", categoryId, userId);

        return categoryMapper.toDto(category);
    }
}
