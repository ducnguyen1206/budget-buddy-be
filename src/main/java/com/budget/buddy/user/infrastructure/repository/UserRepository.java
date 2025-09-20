package com.budget.buddy.user.infrastructure.repository;

import com.budget.buddy.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAddress_Value(String email);
    Optional<User> findByEmailAddress_ValueAndEmailAddress_ActiveAndLocked(String email, boolean active, boolean locked);
    boolean existsByEmailAddress_Value(String email);
}
