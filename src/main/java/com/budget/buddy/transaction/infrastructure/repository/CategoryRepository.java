package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.budget.buddy.transaction.domain.model.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {


    Optional<Category> findByIdentity_NameAndIdentity_Type(String name, CategoryType type);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.userId = :userId")
    Optional<Category> findBydId(Long id, Long userId);
}
