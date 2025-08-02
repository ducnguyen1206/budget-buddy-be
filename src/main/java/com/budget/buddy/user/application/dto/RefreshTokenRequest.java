package com.budget.buddy.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @Schema(description = "JWT accessToken for authenticated user", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "Token must not be empty")
        String refreshToken) {
}
