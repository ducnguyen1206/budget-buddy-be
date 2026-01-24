package com.budget.buddy.transaction.application.dto.threshold;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "Request DTO for threshold transaction inquiry")
public record ThresholdTransactionRequestDTO(
        @NotNull(message = "Category ID is required")
        @Schema(description = "Category ID to filter transactions", example = "3")
        Long categoryId,

        @NotNull(message = "Start date is required")
        @Schema(description = "Start date for filtering transactions", example = "2026-01-05")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @Schema(description = "End date for filtering transactions", example = "2026-02-05")
        LocalDate endDate,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter uppercase code (ISO 4217)")
        @Schema(description = "Currency code", example = "SGD")
        String currency
) {}
