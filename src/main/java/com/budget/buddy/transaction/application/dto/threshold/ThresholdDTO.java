package com.budget.buddy.transaction.application.dto.threshold;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response DTO for Threshold")
public record ThresholdDTO(
        @Schema(description = "Threshold ID", example = "10")
        Long id,

        @Schema(description = "Category ID", example = "5")
        Long categoryId,

        @Schema(description = "Category name", example = "Food & Dining")
        String categoryName,

        @Schema(description = "Threshold amount", example = "500.00")
        BigDecimal threshold,

        @Schema(description = "Currency code", example = "SGD")
        String currency,

        @Schema(description = "Last updated")
        LocalDateTime updatedAt
) {}
