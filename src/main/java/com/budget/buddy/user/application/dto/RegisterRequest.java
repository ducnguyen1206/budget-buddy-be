package com.budget.buddy.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @Schema(description = "Email address of the user", example = "john@example.com")
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email must be a valid email address")
        String email) {
}
