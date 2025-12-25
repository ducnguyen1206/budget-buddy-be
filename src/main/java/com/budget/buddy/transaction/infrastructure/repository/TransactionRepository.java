package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    boolean existsBySourceAccountIdAndUserId(Long accountId, Long userId);

    boolean existsBySourceAccountIdInAndUserId(List<Long> accountIds, Long userId);

    boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);

    List<Transaction> findTransactionBySourceAccountIdInAndUserId(List<Long> accountId, Long userId);

    List<Transaction> findTransactionByCategoryIdAndUserId(Long categoryId, Long userId);
}
