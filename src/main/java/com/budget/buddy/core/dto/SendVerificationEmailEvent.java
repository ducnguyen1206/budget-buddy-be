package com.budget.buddy.core.dto;

import java.time.LocalDateTime;

public record SendVerificationEmailEvent(
        String email,
        String token,
        LocalDateTime expiresAt
) {
}
