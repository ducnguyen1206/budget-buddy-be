package com.budget.buddy.user.infrastructure.repository;

import com.budget.buddy.user.domain.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {

}
