package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.transaction.RetrieveTransactionsParams;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.application.dto.transaction.TransactionFilterCriteria;
import com.budget.buddy.transaction.application.dto.transaction.TransactionPagination;

import java.util.List;

public interface TransactionData {
    void createTransaction(TransactionDTO transactionRequest);

    void deleteTransactionByAccountId(List<Long> accountIds);

    TransactionPagination retrieveTransactions(RetrieveTransactionsParams params, TransactionFilterCriteria filterCriteria);
}
