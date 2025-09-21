package com.budget.buddy.transaction.application.dto.transaction;

import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Transaction details")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    @Schema(description = "Transaction ID", example = "1", hidden = true)
    private Long id;

    @NotBlank(message = "Transaction name is required")
    @Schema(description = "Transaction name", example = "Grocery shopping")
    private String name;

    @NotNull(message = "Amount is required")
    @Schema(description = "Amount", example = "49.99")
    private BigDecimal amount;

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID", example = "1001")
    private Long accountId;

    @NotNull(message = "Category ID is required")
    @Schema(description = "Category ID", example = "2002")
    private Long categoryId;

    @Schema(description = "Notes", example = "From Savings to Checking", hidden = true)
    private String remarks;

    @NotNull(message = "Date is required")
    @Schema(description = "Transaction date", example = "2025-09-01")
    private LocalDate date;

    @Schema(description = "Date formatted as dd-MM-yyyy", example = "18-09-2025", hidden = true)
    private String formattedDate;

    @Schema(description = "Target account ID (for transfers)", example = "1")
    private Long targetAccountId;

    @Schema(description = "Account name", example = "Live Fresh")
    private String sourceAccountName;

    @Schema(description = "Category name", example = "Food")
    private String categoryName;

    @Schema(description = "Currency code", example = "SGD")
    private String currency;

    @Schema(description = "Category type", example = "EXPENSE")
    private CategoryType categoryType;
}
