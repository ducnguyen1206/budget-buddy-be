package com.budget.buddy.transaction.application.dto.budget;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data Transfer Object for Budget")
public record BudgetDTO(
        @Schema(description = "Unique identifier of the category", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotBlank(message = "Category ID is required")
        @Schema(description = "ID of the category", example = "1")
        Long categoryId,

        @Schema(description = "Name of the category", example = "Food", accessMode = Schema.AccessMode.READ_ONLY)
        String categoryName,

        @NotNull(message = "amount is required")
        @Schema(description = "Amount of the budget", example = "49.9")
        BigDecimal amount,

        @Schema(description = "Amount spent from the budget", example = "20.5", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal spentAmount,

        @Schema(description = "Amount remaining in the budget", example = "29.4", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal remainingAmount,

        @NotNull(message = "Currency is required")
        @Pattern(regexp = "^(VND|SGD)$", message = "Currency must be either VND or SGD")
        @Schema(description = "Currency of the budget", example = "SGD")
        String currency
) {
}
