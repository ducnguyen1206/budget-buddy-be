package com.budget.buddy.transaction.application.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetrieveTransactionsParams {
    private Integer page;
    private Integer size;

    // Sorting
    private String sortBy;
    private Sort.Direction direction;
}
