package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsBySourceAccountId(Long accountId);
    boolean existsBySourceAccountIdIn(List<Long> accountIds);
    boolean existsByCategoryId(Long categoryId);
    List<Transaction> findTransactionBySourceAccountIdIn(List<Long> accountId);

    Page<Transaction> findByUserId(Long userId, Pageable pageable);
}
