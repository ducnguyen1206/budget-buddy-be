package com.budget.buddy.transaction.application.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(description = "Accounts by type")
public class AccountRetrieveResponse {
    @Schema(description = "Account type", example = "CASH")
    private String accountType;

    @Schema(description = "List of accounts")
    private List<AccountDTO> accounts;
}
