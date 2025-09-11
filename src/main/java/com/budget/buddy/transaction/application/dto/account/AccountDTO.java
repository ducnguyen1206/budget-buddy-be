package com.budget.buddy.transaction.application.dto.account;

import com.budget.buddy.transaction.domain.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Data Transfer Object for Account")
public record AccountDTO(
        @Schema(description = "Unique identifier of the account", example = "1", hidden = true)
        Long id,

        @Schema(description = "Name of the account", example = "Personal")
        @NotNull(message = "Account name is required")
        String name,

        @Schema(description = "Balance of the account", example = "1000.00")
        @NotNull(message = "Account balance is required")
        BigDecimal balance,

        @Schema(description = "Currency of the account", example = "SGD")
        @NotNull(message = "Account currency is required")
        Currency currency,

        @Schema(description = "Type of the account", example = "CASH")
        @NotNull(message = "Account type is required")
        String type,

        @Schema(description = "Account group ID", example = "1", hidden = true)
        Long accountGroupId
) {
}