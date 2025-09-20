package com.budget.buddy.transaction.application.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class AccountRetrieveResponse {
    private String accountType;
    private List<AccountDTO> accounts;
}
