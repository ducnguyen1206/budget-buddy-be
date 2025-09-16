package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.transaction.RetrieveTransactionsParams;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.domain.model.transaction.Transaction;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TransactionData {
    void createTransaction(TransactionDTO transactionRequest);

    void deleteTransactionByAccountId(List<Long> accountIds);

    Page<Transaction> retrieveTransactions(RetrieveTransactionsParams params);
}
