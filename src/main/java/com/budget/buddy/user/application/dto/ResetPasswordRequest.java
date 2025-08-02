package com.budget.buddy.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @Schema(description = "Email address of the user", example = "john@example.com")
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email must be a valid email address")
        String email,

        @Schema(description = "Password for the account", example = "P@ssw0rd")
        @NotBlank(message = "Password must not be empty")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$",
                message = "Password must be at least 8 characters long and include one uppercase, one lowercase, one digit, and one special character"
        )
        String password,

        @Schema(description = "Re-Enter password for the account", example = "P@ssw0rd")
        @NotBlank(message = "Re-Enter password must not be empty")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$",
                message = "Re-Enter password must be the same as password"
        )
        String reenterPassword
) {}