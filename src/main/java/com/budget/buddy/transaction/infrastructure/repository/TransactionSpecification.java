package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.application.dto.transaction.TransactionFilterCriteria;
import com.budget.buddy.transaction.domain.model.transaction.Transaction;
import org.springframework.data.jpa.domain.Specification;

public interface TransactionSpecification {
    Specification<Transaction> buildSpecification(TransactionFilterCriteria criteria, String sort);
}
