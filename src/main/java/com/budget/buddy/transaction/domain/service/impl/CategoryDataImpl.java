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
import com.budget.buddy.transaction.infrastructure.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryDataImpl implements CategoryData {
    private static final Logger logger = LogManager.getLogger(CategoryDataImpl.class);
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final TransactionUtils transactionUtils;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryRequest) {
        Long userId = transactionUtils.getCurrentUserId();

        logger.info("Creating category: name='{}', for user user Id='{}'",
                categoryRequest.name(), userId);

        var existingOpt = categoryRepository.findByUserIdAndIdentity_Name(userId, categoryRequest.name());
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            logger.info("Category already exists with id={} for userId={}, skipping creation", existing.getId(), userId);
            return categoryMapper.toDto(existing);
        }

        CategoryVO categoryIdentity = new CategoryVO(categoryRequest.name());
        Category category = new Category(categoryIdentity, userId);


        category = categoryRepository.save(category);
        logger.info("Category created with id={} for userId={}", category.getId(), userId);

        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryDTO getCategory(Long categoryId) {
        Long userId = transactionUtils.getCurrentUserId();

        Category category = categoryRepository.findBydId(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        logger.info("Retrieved category id={}", categoryId);
        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDTO> getCategories() {
        Long userId = transactionUtils.getCurrentUserId();

        List<Category> categories = categoryRepository.findAllByUserId(userId);
        logger.info("Retrieved {} categories for", categories.size());

        return categories.stream().map(categoryMapper::toDto).toList();
    }

    @Transactional
    @Override
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);

        logger.info("Deleted category id={} for", categoryId);
    }

    @Transactional
    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryRequest) {
        var existingOpt = categoryRepository.findByUserIdAndIdentity_Name(transactionUtils.getCurrentUserId(), categoryRequest.name());
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            logger.info("Category already exists with id={} for skipping update", existing.getId());
            return categoryMapper.toDto(existing);
        }

        Long userId = transactionUtils.getCurrentUserId();

        Category category = categoryRepository.findBydId(categoryId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        logger.info("Updating category id={} for", categoryId);

        category.setIdentity(new CategoryVO(categoryRequest.name()));
        category = categoryRepository.save(category);
        logger.info("Updated category id={} for", categoryId);

        return categoryMapper.toDto(category);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isTransactionExistedByCategoryId(Long accountId) {
        Long userId = transactionUtils.getCurrentUserId();
        return transactionRepository.existsByCategoryIdAndUserId(accountId, userId);
    }
}
