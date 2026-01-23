package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.subscription.Subscription;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"account", "account.accountTypeGroup"})
    List<Subscription> findAllByUserIdOrderByPayDayAscIdAsc(Long userId);

    void deleteAllByIdInAndUserId(Collection<Long> ids, Long userId);
}
