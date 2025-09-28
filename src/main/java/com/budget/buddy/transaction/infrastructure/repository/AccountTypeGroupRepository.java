package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.account.AccountTypeGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface AccountTypeGroupRepository extends JpaRepository<AccountTypeGroup, Long> {
    Optional<AccountTypeGroup> findByName(String name);

    @EntityGraph(attributePaths = {"accounts"})
    @NonNull
    List<AccountTypeGroup> findAll();

    @Query("SELECT a FROM AccountTypeGroup a WHERE a.id = :id AND a.userId = :userId")
    Optional<AccountTypeGroup> findBydId(Long id, Long userId);
}
