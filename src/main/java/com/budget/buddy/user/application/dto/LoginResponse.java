package com.budget.buddy.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "JWT accessToken for authenticated user", example = "eyJhbGciOiJIUzI1NiJ9...") String token,
        @Schema(description = "JWT refresh token for authenticated user", example = "eyJhbGciOiJIUzI1NiJ9...") String refreshToken
) {}
