package com.budget.buddy.transaction.application.service;

import com.budget.buddy.transaction.application.dto.transaction.RetrieveTransactionsParams;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.application.dto.transaction.TransactionFilterCriteria;
import com.budget.buddy.transaction.application.dto.transaction.TransactionPagination;

import java.util.List;

public interface TransactionService {
    void createTransaction(TransactionDTO transactionRequest);

    void createTransactions(List<TransactionDTO> transactionRequests);

    TransactionPagination retrieveTransactions(RetrieveTransactionsParams params, TransactionFilterCriteria filterCriteria);

    void updateTransaction(Long transactionId, TransactionDTO transactionRequest);

    void deleteTransaction(Long transactionId);
}
