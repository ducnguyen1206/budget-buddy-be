package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.application.mapper.CategoryMapper;
import com.budget.buddy.transaction.domain.model.category.Category;
import com.budget.buddy.transaction.domain.service.CategoryData;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.domain.vo.CategoryVO;
import com.budget.buddy.transaction.infrastructure.repository.CategoryRepository;
import com.budget.buddy.user.application.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryDataImpl implements CategoryData {
    private static final Logger logger = LogManager.getLogger(CategoryDataImpl.class);
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final TransactionUtils transactionUtils;

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryRequest) {
        Long userId = transactionUtils.getCurrentUserId();

        logger.info("Creating category: name='{}', type='{}' for user user Id='{}'",
                categoryRequest.name(), categoryRequest.type(), userId);


        var existingOpt = categoryRepository.findByIdentity_NameAndIdentity_Type(categoryRequest.name(), categoryRequest.type());
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

    @Override
    public CategoryDTO getCategory(Long categoryId) {
        Long userId = transactionUtils.getCurrentUserId();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        logger.info("Retrieved category id={} for userId={}", categoryId, userId);
        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDTO> getCategories() {
        Long userId = transactionUtils.getCurrentUserId();

        List<Category> categories = categoryRepository.findAll();
        logger.info("Retrieved {} categories for userId={}", categories.size(), userId);

        return categories.stream().map(categoryMapper::toDto).toList();
    }

    @Transactional
    @Override
    public void deleteCategory(Long categoryId) {
        Long userId = transactionUtils.getCurrentUserId();

        categoryRepository.deleteById(categoryId);

        logger.info("Deleted category id={} for userId={}", categoryId, userId);
    }

    @Transactional
    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest) {
        Long userId = transactionUtils.getCurrentUserId();

        var existingOpt = categoryRepository.findByIdentity_NameAndIdentity_Type(categoryRequest.name(), categoryRequest.type());
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            logger.info("Category already exists with id={} for userId={}, skipping update", existing.getId(), userId);
            return categoryMapper.toDto(existing);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        logger.info("Updating category id={} for userId={}", categoryId, userId);

        category.setIdentity(new CategoryVO(categoryRequest.name(), categoryRequest.type()));
        category = categoryRepository.save(category);
        logger.info("Updated category id={} for userId={}", categoryId, userId);

        return categoryMapper.toDto(category);
    }
}
