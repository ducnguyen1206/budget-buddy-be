package com.budget.buddy.core.config.exception;

import io.swagger.v3.oas.annotations.media.Schema;


public record ErrorResponse(@Schema(description = "Unique error code", example = "AUTH_001") String errorCode,
                            @Schema(description = "Error message for the user", example = "Invalid email or password") String message) {
}