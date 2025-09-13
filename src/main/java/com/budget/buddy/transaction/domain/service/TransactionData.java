package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;

public interface TransactionData {
    void createTransaction(TransactionDTO transactionRequest);
}
