package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.budget.buddy.transaction.domain.model.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    Optional<Category> findByIdentity_NameAndIdentity_Type(String name, CategoryType type);
}
