package com.budget.buddy.transaction.application.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(description = "Available account types")
public class AccountTypeRetrieveResponse {
    @Schema(description = "List of account types", example = "[\"CASH\",\"BANK\",\"CREDIT_CARD\"]")
    private List<String> accountTypes;
}
