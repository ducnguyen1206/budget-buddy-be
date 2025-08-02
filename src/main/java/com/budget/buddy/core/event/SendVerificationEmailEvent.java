package com.budget.buddy.core.event;

import java.time.LocalDateTime;

public record SendVerificationEmailEvent(
        String email,
        String token,
        LocalDateTime expiresAt
) {
}
