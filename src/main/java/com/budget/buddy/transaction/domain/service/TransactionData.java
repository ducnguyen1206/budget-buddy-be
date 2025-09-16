package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;

import java.util.List;

public interface TransactionData {
    void createTransaction(TransactionDTO transactionRequest);

    void deleteTransactionByAccountId(List<Long> accountIds);
}
