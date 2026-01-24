package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.threshold.ThresholdDTO;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.category.Category;
import com.budget.buddy.transaction.domain.model.threshold.Threshold;
import com.budget.buddy.transaction.domain.service.ThresholdDataService;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.infrastructure.repository.CategoryRepository;
import com.budget.buddy.transaction.infrastructure.repository.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThresholdDataServiceImpl implements ThresholdDataService {
    private static final Logger logger = LogManager.getLogger(ThresholdDataServiceImpl.class);

    private final ThresholdRepository thresholdRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public ThresholdDTO create(Long categoryId, BigDecimal threshold, String currency) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Creating threshold for userId='{}' with categoryId='{}', threshold='{}', currency='{}'", userId, categoryId, threshold, currency);

        Category category = validateAndGetOwnedCategory(userId, categoryId);

        Threshold entity = new Threshold(userId, category, threshold, Currency.valueOf(currency));
        return toDTO(thresholdRepository.save(entity));
    }

    @Override
    public ThresholdDTO view(Long id) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Viewing threshold id='{}' for userId='{}'", id, userId);
        Threshold threshold = thresholdRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.THRESHOLD_NOT_FOUND));
        return toDTO(threshold);
    }

    @Override
    public List<ThresholdDTO> viewAll() {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Viewing all thresholds for userId='{}'", userId);
        return thresholdRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public ThresholdDTO update(Long id, Long categoryId, BigDecimal threshold, String currency) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Updating threshold id='{}' for userId='{}'", id, userId);

        Threshold existing = thresholdRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.THRESHOLD_NOT_FOUND));

        if (!existing.getCategory().getId().equals(categoryId)) {
            Category category = validateAndGetOwnedCategory(userId, categoryId);
            existing.setCategory(category);
        }

        existing.setThreshold(threshold);
        existing.setCurrency(Currency.valueOf(currency));
        return toDTO(thresholdRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Deleting threshold id='{}' for userId='{}'", id, userId);
        Threshold existing = thresholdRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.THRESHOLD_NOT_FOUND));
        thresholdRepository.delete(existing);
    }

    @Override
    public ThresholdDTO getByCategoryIdAndCurrency(Long categoryId, String currency) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Getting threshold for userId='{}', categoryId='{}', currency='{}'", userId, categoryId, currency);
        Threshold threshold = thresholdRepository.findByCategoryIdAndUserIdAndCurrency(categoryId, userId, Currency.valueOf(currency))
                .orElse(null);
        return threshold == null ? null : toDTO(threshold);
    }

    private Category validateAndGetOwnedCategory(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ConflictException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private ThresholdDTO toDTO(Threshold t) {
        return new ThresholdDTO(
                t.getId(),
                t.getCategory().getId(),
                t.getCategory().getIdentity().getName(),
                t.getThreshold(),
                t.getCurrency().name(),
                t.getLastModifiedDate()
        );
    }
}
