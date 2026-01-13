package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.installment.Installment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    Optional<Installment> findByIdAndUserIdOrderByIdAsc(Long id, Long userId);

    @EntityGraph(attributePaths = {"account", "account.accountTypeGroup"})
    List<Installment> findAllByUserIdOrderByDueDateAscIdAsc(Long userId);

    void deleteAllByIdInAndUserId(Collection<Long> ids, Long userId);
}
