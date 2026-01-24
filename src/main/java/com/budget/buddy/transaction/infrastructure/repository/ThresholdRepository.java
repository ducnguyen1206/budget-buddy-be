package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.threshold.Threshold;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThresholdRepository extends JpaRepository<Threshold, Long> {

    @EntityGraph(attributePaths = {"category"})
    Optional<Threshold> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"category"})
    List<Threshold> findAllByUserIdOrderByIdAsc(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);

    @EntityGraph(attributePaths = {"category"})
    Optional<Threshold> findByCategoryIdAndUserIdAndCurrency(Long categoryId, Long userId, com.budget.buddy.transaction.domain.enums.Currency currency);
}
