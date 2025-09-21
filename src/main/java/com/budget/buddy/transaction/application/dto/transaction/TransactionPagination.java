package com.budget.buddy.transaction.application.dto.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Paginated list of transactions")
public class TransactionPagination {
    @Schema(description = "Paging info")
    private Pagination pagination;

    @Schema(description = "List of transactions")
    private List<TransactionDTO> transactions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Paging info")
    public static class Pagination {
        @Schema(description = "Current page number", example = "1")
        private int page;

        @Schema(description = "Page size", example = "20")
        private int size;

        @Schema(description = "Total items", example = "125")
        private long totalElements;

        @Schema(description = "Total pages", example = "7")
        private int totalPages;
    }
}
