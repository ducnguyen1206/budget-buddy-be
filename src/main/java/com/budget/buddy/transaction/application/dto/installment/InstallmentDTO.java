package com.budget.buddy.transaction.application.dto.installment;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data Transfer Object for Installment")
public record InstallmentDTO(
        @Schema(description = "Installment ID", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotNull(message = "Account ID is required")
        @Schema(description = "Linked account ID", example = "3")
        Long accountId,

        @Schema(description = "Linked account name", example = "Main Checking", accessMode = Schema.AccessMode.READ_ONLY)
        String accountName,

        @NotBlank(message = "Installment name is required")
        @Schema(description = "Installment name", example = "Car Loan")
        String name,

        @NotNull(message = "Total amount is required")
        @Schema(description = "Total amount of the installment", example = "50000.00")
        BigDecimal totalAmount,

        @NotNull(message = "Amount paid is required")
        @Schema(description = "Amount already paid", example = "10000.00")
        BigDecimal amountPaid,

        @Schema(description = "Outstanding amount (calculated)", example = "40000.00", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal outstandingAmount,

        @NotNull(message = "Due date is required")
        @Schema(description = "Due date for the installment", example = "2026-12-31")
        LocalDate dueDate,

        @Schema(description = "Currency (from account)", example = "SGD", accessMode = Schema.AccessMode.READ_ONLY)
        String currency,

        @Schema(description = "Last updated", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime updatedAt
) {}
