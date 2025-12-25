package com.budget.buddy.transaction.application.controller;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.application.dto.account.AccountTypeRetrieveResponse;
import com.budget.buddy.transaction.application.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Account Management", description = "CRUD APIs for managing user accounts and account types")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create a new account", description = "Creates an account for the authenticated user.", responses = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PostMapping
    public ResponseEntity<Void> createAccount(@Valid @RequestBody AccountDTO request) {
        accountService.creteAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "List all accounts", description = "Returns all accounts owned by the authenticated user.", responses = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = AccountRetrieveResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content())
    })
    @GetMapping
    public ResponseEntity<List<AccountRetrieveResponse>> retrieveAccounts() {
        List<AccountRetrieveResponse> accounts = accountService.retrieveAccounts();
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Get an account by ID", description = "Returns details of the specified account if it belongs to the authenticated user.", responses = {
            @ApiResponse(responseCode = "200", description = "Account retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccountRetrieveResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content())
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountRetrieveResponse> retrieveAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.retrieveAccount(accountId));
    }

    @Operation(summary = "Update an existing account", description = "Updates the specified account if it belongs to the authenticated user.", responses = {
            @ApiResponse(responseCode = "204", description = "Account updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PutMapping("/{accountId}")
    public ResponseEntity<Void> updateAccount(@PathVariable Long accountId, @Valid @RequestBody AccountDTO request) {
        accountService.updateAccount(accountId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete an account by ID", description = "Deletes the specified account if it belongs to the authenticated user.", responses = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content())
    })
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List account types", description = "Returns supported account types and their groups.", responses = {
            @ApiResponse(responseCode = "200", description = "Account types retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccountTypeRetrieveResponse.class)))
    })
    @GetMapping("/types")
    public ResponseEntity<AccountTypeRetrieveResponse> retrieveAccountTypes() {
        AccountTypeRetrieveResponse accountTypes = accountService.retrieveAccountTypes();
        return ResponseEntity.ok(accountTypes);
    }

    @Operation(summary = "Delete an account type group", description = "Deletes an account type group by its ID.", responses = {
            @ApiResponse(responseCode = "204", description = "Account type group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account type group not found", content = @Content())
    })
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Void> deleteAccountTypeGroup(@PathVariable Long groupId) {
        accountService.deleteAccountTypeGroup(groupId);
        return ResponseEntity.noContent().build();
    }
}
