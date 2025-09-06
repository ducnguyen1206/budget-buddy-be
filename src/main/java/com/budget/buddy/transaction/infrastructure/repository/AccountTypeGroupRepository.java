package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.account.AccountTypeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTypeGroupRepository extends JpaRepository<AccountTypeGroup, Long> {
    Optional<AccountTypeGroup> findByName(String name);
}
