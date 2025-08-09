package com.budget.buddy.user.infrastructure.repository;

import com.budget.buddy.user.domain.model.Session;
import com.budget.buddy.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByRefreshToken(String token);
    Optional<Session> findByUserId(Long userId);
}
