package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.application.dto.budget.BudgetDTO;
import com.budget.buddy.transaction.domain.model.budget.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    boolean existsByCategoryIdAndMoney_Currency(Long categoryId, String currency);

    @Query("SELECT b FROM Budget b WHERE b.id = :id AND b.userId = :userId")
    Optional<Budget> findBydId(Long id, Long userId);

    @Query("""
            SELECT b.id                                                                 AS id,
                   b.category.id                                                        AS caegoryID,
                   c.identity.name                                                      AS categoryName,
                   MAX(b.money.amount)                                                  AS amount,
                   COALESCE(SUM(CASE WHEN t.amount < 0 THEN t.amount ELSE 0 END), 0)    AS spentAmount,
                   COALESCE(MAX(b.money.amount), 0) -
                   COALESCE(SUM(CASE WHEN t.amount < 0 THEN -t.amount ELSE 0 END), 0)   AS remainingAmount,
                   b.money.currency                                                     AS currency,
                   b.lastModifiedDate                                                   AS updatedAt
            FROM Budget b
                     JOIN Category c ON c.id = b.category.id
                     LEFT JOIN Transaction t ON t.category.id = c.id AND t.sourceAccount.money.currency = b.money.currency
                     LEFT JOIN Account a ON a.id = t.sourceAccount.id
            GROUP BY b.id, b.category.id, c.identity.name, b.money.currency
            """)
    List<BudgetDTO> findAllBudgetsForUser();

    @Query("""
            SELECT b.id                                                                 AS id,
                   b.category.id                                                        AS caegoryID,
                   c.identity.name                                                      AS categoryName,
                   MAX(b.money.amount)                                                  AS amount,
                   COALESCE(SUM(CASE WHEN t.amount < 0 THEN t.amount ELSE 0 END), 0)    AS spentAmount,
                   COALESCE(MAX(b.money.amount), 0) -
                   COALESCE(SUM(CASE WHEN t.amount < 0 THEN -t.amount ELSE 0 END), 0)   AS remainingAmount,
                   b.money.currency                                                     AS currency
            FROM Budget b
                     JOIN Category c ON c.id = b.category.id
                     LEFT JOIN Transaction t ON t.category.id = c.id AND t.sourceAccount.money.currency = b.money.currency
                     LEFT JOIN Account a ON a.id = t.sourceAccount.id
            WHERE b.id = :id
            GROUP BY b.id, b.category.id, c.identity.name, b.money.currency
            """)
    Optional<BudgetDTO> findBudgetDTOByIdAndUserId(Long id);
}
