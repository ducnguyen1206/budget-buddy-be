package com.budget.buddy.transaction.application.dto.threshold;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response DTO for threshold transaction inquiry")
public record ThresholdTransactionResponseDTO(
        @Schema(description = "Category name", example = "Food")
        String category,

        @Schema(description = "Category ID", example = "1")
        Long categoryId,

        @Schema(description = "Currency code", example = "SGD")
        String currency,

        @Schema(description = "Threshold amount for the category", example = "20.00")
        BigDecimal threshold,

        @Schema(description = "List of daily transaction summaries with threshold comparison")
        List<DailyThresholdSummary> transactions
) {
    @Schema(description = "Daily summary of transactions compared to threshold")
    public record DailyThresholdSummary(
            @Schema(description = "Transaction date", example = "2026-01-23")
            LocalDate date,

            @Schema(description = "Total amount of transactions for this date", example = "-25.30")
            BigDecimal totalAmount,

            @Schema(description = "Threshold amount for the category", example = "20.00")
            BigDecimal threshold,

            @Schema(description = "Amount exceeded (positive means within budget, negative means over budget)", example = "-5.30")
            BigDecimal exceededAmount
    ) {}
}
