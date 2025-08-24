package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.budget.buddy.transaction.domain.model.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    List<Category> findAllByUserId(Long userId);

    Optional<Category> findByUserIdAndIdentity_NameAndIdentity_Type(Long userId, String name, CategoryType type);

    long deleteByIdAndUserId(Long id, Long userId);
}
