package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    boolean existsBySourceAccountId(Long accountId);

    boolean existsBySourceAccountIdIn(List<Long> accountIds);

    boolean existsByCategoryId(Long categoryId);

    List<Transaction> findTransactionBySourceAccountIdIn(List<Long> accountId);
}
