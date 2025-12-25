package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.application.dto.transaction.RetrieveTransactionsParams;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.application.dto.transaction.TransactionFilterCriteria;
import com.budget.buddy.transaction.application.dto.transaction.TransactionPagination;
import com.budget.buddy.transaction.application.service.TransactionService;
import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.budget.buddy.transaction.domain.service.AccountData;
import com.budget.buddy.transaction.domain.service.TransactionData;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionData transactionData;
    private final AccountData accountData;
    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);

    @Override
    public void createTransaction(TransactionDTO transactionRequest) {
        accountData.checkAccountExists(transactionRequest.getAccountId());

        // save transaction
        validateTransferInfo(transactionRequest);
        transactionData.createTransaction(transactionRequest);
    }

    private void validateTransferInfo(TransactionDTO transactionRequest) {
        CategoryType categoryType = transactionRequest.getCategoryType();

        if (!CategoryType.TRANSFER.equals(categoryType)) {
            return;
        }

        Long sourceAccountId = transactionRequest.getAccountId();
        Long targetAccountId = transactionRequest.getTargetAccountId();

        if (targetAccountId == null || sourceAccountId.equals(targetAccountId)) {
            logger.info("toAccountId is null for transaction type transfer");
            throw new BadRequestException(ErrorCode.INVALID_REQUEST_DATA);
        }

        List<AccountRetrieveResponse> accounts = accountData.retrieveAccountByIdList(List.of(sourceAccountId, targetAccountId));

        // Flatten accounts by type into a map of accountId -> AccountDTO
        Map<Long, AccountDTO> accountMap = accounts.stream()
                .flatMap(resp -> resp.getAccounts().stream())
                .collect(Collectors.toMap(AccountDTO::id, a -> a));

        AccountDTO fromAccount = accountMap.get(sourceAccountId);
        AccountDTO toAccount = accountMap.get(targetAccountId);

        if (fromAccount == null || toAccount == null) {
            logger.info("Invalid account ID list. One or both accounts do not exist. fromId='{}', toId='{}'", sourceAccountId, targetAccountId);
            throw new BadRequestException(ErrorCode.INVALID_REQUEST_DATA);
        }

        // Ensure the same currency for transfer
        if (!fromAccount.currency().equals(toAccount.currency())) {
            logger.info("Transfer failed: currency mismatch. from='{}', to='{}'", fromAccount.currency(), toAccount.currency());
            throw new ConflictException(ErrorCode.CURRENCY_TRANSFER_SHOULD_BE_THE_SAME);
        }

        // Check valid balance
        BigDecimal sourceBalance = fromAccount.balance();
        BigDecimal requestAmount = transactionRequest.getAmount();
        if (sourceBalance.compareTo(requestAmount) < 0) {
            throw new ConflictException(ErrorCode.SOURCE_ACCOUNT_BALANCE_NOT_ENOUGH_MONEY);
        }
    }


    @Override
    public TransactionPagination retrieveTransactions(RetrieveTransactionsParams params, TransactionFilterCriteria filterCriteria) {
        logger.info("Retrieving transactions with params: {}", params);
        return transactionData.retrieveTransactions(params, filterCriteria);
    }

    @Override
    public void updateTransaction(Long transactionId, TransactionDTO transactionRequest) {
        // For transfers, updating requires handling mirror entries which is not supported at the moment
        if (CategoryType.TRANSFER.equals(transactionRequest.getCategoryType())) {
            throw new BadRequestException(ErrorCode.INVALID_REQUEST_DATA);
        }

        // Validate account exists
        accountData.checkAccountExists(transactionRequest.getAccountId());

        // Delegate to the domain layer for persistence and ownership checks
        transactionData.updateTransaction(transactionId, transactionRequest);
    }
}
