package com.budget.buddy.user.domain.impl;

import com.budget.buddy.core.BaseEntity;
import jakarta.persistence.Column;

public class User extends BaseEntity {


    // Only user for new account
    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;
}
