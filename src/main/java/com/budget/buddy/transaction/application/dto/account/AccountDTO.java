package com.budget.buddy.transaction.application.dto.account;

import com.budget.buddy.transaction.domain.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Account details")
public record AccountDTO(
        @Schema(description = "Account ID", example = "1", hidden = true)
        Long id,

        @Schema(description = "Account name", example = "Personal")
        @NotNull(message = "Account name is required")
        String name,

        @Schema(description = "Balance", example = "1000.00")
        @NotNull(message = "Account balance is required")
        BigDecimal balance,

        @Schema(description = "Currency code", example = "SGD")
        @NotNull(message = "Account currency is required")
        Currency currency,

        @Schema(description = "Account type", example = "CASH")
        @NotNull(message = "Account type is required")
        String type,

        @Schema(description = "Account group ID", example = "1")
        Long accountGroupId
) {
}