package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.saving.Saving;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SavingRepository extends JpaRepository<Saving, Long> {

    Optional<Saving> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"account", "account.accountTypeGroup"})
    List<Saving> findAllByUserId(Long userId);

    void deleteAllByIdInAndUserId(Collection<Long> ids, Long userId);
}
