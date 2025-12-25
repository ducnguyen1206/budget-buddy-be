package com.budget.buddy.transaction.application.controller;

import com.budget.buddy.transaction.application.dto.transaction.RetrieveTransactionsParams;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.application.dto.transaction.TransactionFilterCriteria;
import com.budget.buddy.transaction.application.dto.transaction.TransactionPagination;
import com.budget.buddy.transaction.application.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Transaction Management", description = "APIs to create and search user transactions")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/transaction")
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Create a new transaction", description = "Creates a transaction for the authenticated user.", responses = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content()),
            @ApiResponse(responseCode = "409", description = "Conflict while creating transaction", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> createTransaction(@Valid @RequestBody TransactionDTO request) {
        transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Search and paginate transactions", description = "Returns a paged list of transactions. Use query parameters for pagination and sorting (page, size). Optionally provide filter criteria in the request body to filter by fields like date range, category, account, amount, etc.", responses = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TransactionPagination.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PostMapping("/inquiry")
    public ResponseEntity<TransactionPagination> retrieveTransactions(
            @Parameter(description = "Zero-based page index for pagination", example = "0")
            @RequestParam(value = "page", required = false) @Min(0) Integer page,
            @Parameter(description = "Page size (min 1, max 20)", example = "20")
            @RequestParam(value = "size", required = false) @Min(1) @Max(20) Integer size,
            @Valid @RequestBody(required = false) TransactionFilterCriteria filterCriteria) {
        RetrieveTransactionsParams request = new RetrieveTransactionsParams(page, size);
        return ResponseEntity.ok(transactionService.retrieveTransactions(request, filterCriteria));
    }

    @Operation(summary = "Update a transaction", description = "Updates an existing transaction for the authenticated user.", responses = {
            @ApiResponse(responseCode = "204", description = "Transaction updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content())
    })
    @PutMapping(path = "/{id}")
    public ResponseEntity<Void> updateTransaction(
            @PathVariable("id") Long transactionId,
            @Valid @RequestBody TransactionDTO request) {
        transactionService.updateTransaction(transactionId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a transaction", description = "Deletes an existing transaction for the authenticated user.", responses = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., cannot delete TRANSFER)", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content())
    })
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("id") Long transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }
}
