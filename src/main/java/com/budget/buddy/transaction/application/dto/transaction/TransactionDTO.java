package com.budget.buddy.transaction.application.dto.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Schema(description = "Data Transfer Object for Transaction")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    @Schema(description = "Unique identifier of the account", example = "1", hidden = true)
    private Long id;

    @NotBlank(message = "Transaction name is required")
    @Schema(description = "Name or description of the transaction", example = "Grocery shopping")
    private String name;

    @NotNull(message = "Amount is required")
    @Schema(description = "Transaction amount", example = "49.99")
    private BigDecimal amount;

    @NotNull(message = "Account ID is required")
    @Schema(description = "Identifier of the account associated with the transaction", example = "1001")
    private Long accountId;

    @NotNull(message = "Category ID is required")
    @Schema(description = "Identifier of the category associated with the transaction", example = "2002")
    private Long categoryId;

    @JsonIgnore
    @Schema(description = "Optional transfer information if the transaction is a transfer", example = "From Savings to Checking", hidden = true)
    private String transferInfo;

    @NotNull(message = "Date is required")
    @Schema(description = "Date of the transaction", example = "2025-09-01")
    private LocalDate date;

    @Schema(description = "Optional to account ID for transfer Info", example = "1")
    private Long targetAccountId;
}
