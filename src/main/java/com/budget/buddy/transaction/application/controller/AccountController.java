package com.budget.buddy.transaction.application.controller;

import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.application.dto.account.AccountTypeRetrieveResponse;
import com.budget.buddy.transaction.application.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Account Management", description = "Endpoints for account management module")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Endpoint for creating a new account", responses = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PostMapping
    public ResponseEntity<Void> createAccount(@Valid @RequestBody AccountDTO request) {
        accountService.creteAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Endpoint for retrieving all accounts", responses = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content())
    })
    @GetMapping
    public ResponseEntity<List<AccountRetrieveResponse>> retrieveAccounts() {
        List<AccountRetrieveResponse> accounts = accountService.retrieveAccounts();
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Endpoint for retrieving an account by id", responses = {
            @ApiResponse(responseCode = "200", description = "Account retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content())
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<Object> retrieveAccount(@PathVariable("accountId") Long accountId) {
        return ResponseEntity.ok(accountService.retrieveAccount(accountId));
    }

    @Operation(summary = "Endpoint for updating an account", responses = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PutMapping("/{accountId}")
    public ResponseEntity<Void> updateAccount(@PathVariable("accountId") Long accountId, @Valid @RequestBody AccountDTO request) {
        accountService.updateAccount(accountId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Endpoint for deleting an account", responses = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content())
    })
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("accountId") Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Endpoint for retrieving all account types", responses = {
            @ApiResponse(responseCode = "200", description = "Account types retrieved successfully")
    })
    @GetMapping("/types")
    public ResponseEntity<AccountTypeRetrieveResponse> retrieveAccountTypes() {
        AccountTypeRetrieveResponse accountTypes = accountService.retrieveAccountTypes();
        return ResponseEntity.ok(accountTypes);
    }

    @Operation(summary = "Endpoint for deleting an account type group", responses = {
            @ApiResponse(responseCode = "204", description = "Account type group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account type group not found", content = @Content())
    })
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Void> deleteAccountTypeGroup(@PathVariable("groupId") Long groupId) {
        accountService.deleteAccountTypeGroup(groupId);
        return ResponseEntity.noContent().build();
    }
}
