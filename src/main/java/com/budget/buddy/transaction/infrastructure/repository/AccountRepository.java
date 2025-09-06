package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
