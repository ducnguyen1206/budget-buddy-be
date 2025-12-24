package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {


    Optional<Category> findByUserIdAndIdentity_Name(Long userId, String name);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.userId = :userId")
    Optional<Category> findBydId(Long id, Long userId);

    List<Category> findAllByUserId(Long userId);
}
