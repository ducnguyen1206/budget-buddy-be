package com.budget.buddy.user.infrastructure.repository;

import com.budget.buddy.user.domain.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
    Optional<UserVerification> findByVerificationToken_valueAndVerificationToken_ExpiresAtAfter(String token, LocalDateTime now);

    Optional<UserVerification> findByVerificationToken_value(String token);

    Optional<UserVerification> findByUserId(Long userId);
}
