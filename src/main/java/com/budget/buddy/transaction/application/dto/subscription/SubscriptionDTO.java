package com.budget.buddy.transaction.application.dto.subscription;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data Transfer Object for Subscription")
public record SubscriptionDTO(
        @Schema(description = "Subscription ID", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotNull(message = "Account ID is required")
        @Schema(description = "Linked account ID", example = "3")
        Long accountId,

        @Schema(description = "Linked account name", example = "Main Checking", accessMode = Schema.AccessMode.READ_ONLY)
        String accountName,

        @NotBlank(message = "Subscription name is required")
        @Schema(description = "Subscription name", example = "Netflix")
        String name,

        @NotNull(message = "Amount is required and must be positive and more than 0")
        @Schema(description = "Subscription amount", example = "15.99")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Pay day is required")
        @Min(value = 1, message = "Pay day must be between 1 and 31")
        @Max(value = 31, message = "Pay day must be between 1 and 31")
        @Schema(description = "Day of month for payment", example = "15")
        Integer payDay,

        @Schema(description = "Currency", example = "SGD", accessMode = Schema.AccessMode.READ_ONLY)
        String currency,

        @Schema(description = "Last updated", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime updatedAt
) {}
