package com.budget.buddy.user.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "JWT accessToken for authenticated user", example = "eyJhbGciOiJIUzI1NiJ9...") String token,

        @JsonIgnore
        @Schema(description = "JWT refresh token for authenticated user", example = "eyJhbGciOiJIUzI1NiJ9...", hidden = true) String refreshToken
) {}
