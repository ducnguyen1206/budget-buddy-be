package com.budget.buddy.transaction.application.dto.threshold;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "Request DTO for creating or updating a Threshold")
public record ThresholdRequestDTO(
        @NotNull(message = "Category ID is required")
        @Schema(description = "Category ID", example = "5")
        Long categoryId,

        @NotNull(message = "Threshold amount is required")
        @PositiveOrZero(message = "Threshold must be positive or zero")
        @Schema(description = "Threshold amount", example = "500.00")
        BigDecimal threshold,

        @NotBlank(message = "Currency is required")
        @Schema(description = "Currency code", example = "SGD")
        String currency
) {}
