package com.budget.buddy.transaction.application.service;

import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;

public interface TransactionService {
    void createTransaction(TransactionDTO transactionRequest);
}
