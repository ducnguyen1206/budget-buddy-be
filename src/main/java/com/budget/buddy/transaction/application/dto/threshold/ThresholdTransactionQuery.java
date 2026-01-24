package com.budget.buddy.transaction.application.dto.threshold;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ThresholdTransactionQuery(
        Long categoryId,
        LocalDate startDate,
        LocalDate endDate,
        String currency,
        BigDecimal threshold
) {}
