package com.budget.buddy.transaction.application.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionPagination {
    private Pagination pagination;
    private List<TransactionDTO> transactions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pagination {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
