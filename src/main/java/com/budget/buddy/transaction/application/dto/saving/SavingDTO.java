package com.budget.buddy.transaction.application.dto.saving;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data Transfer Object for Saving goal")
public record SavingDTO(
        @Schema(description = "Saving ID", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotNull(message = "Account ID is required")
        @Schema(description = "Linked account ID", example = "3")
        Long accountId,

        @Schema(description = "Linked account name", example = "Main Checking", accessMode = Schema.AccessMode.READ_ONLY)
        String accountName,

        @NotBlank(message = "Saving name is required")
        @Schema(description = "Saving name", example = "Vacation Fund")
        String name,

        @NotNull(message = "Amount is required")
        @Schema(description = "Saving amount", example = "2000.00")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        @Pattern(regexp = "^(VND|SGD)$", message = "Currency must be either VND or SGD")
        @Schema(description = "Currency", example = "SGD")
        String currency,

        @NotNull(message = "Date is required")
        @Schema(description = "Date of the saving goal", example = "2026-01-15")
        LocalDate date,

        @Schema(description = "Notes", example = "Save monthly from salary")
        String notes,

        @Schema(description = "Last updated", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime updatedAt
) {}
