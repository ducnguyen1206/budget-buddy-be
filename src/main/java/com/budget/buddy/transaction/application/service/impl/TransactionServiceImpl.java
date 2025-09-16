package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.application.dto.transaction.RetrieveTransactionsParams;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.application.dto.transaction.TransactionPagination;
import com.budget.buddy.transaction.application.service.TransactionService;
import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.transaction.Transaction;
import com.budget.buddy.transaction.domain.service.AccountData;
import com.budget.buddy.transaction.domain.service.CategoryData;
import com.budget.buddy.transaction.domain.service.TransactionData;
import com.budget.buddy.transaction.infrastructure.view.AccountFlatView;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionData transactionData;
    private final CategoryData categoryData;
    private final AccountData accountData;
    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);

    @Override
    public void createTransaction(TransactionDTO transactionRequest) {
        CategoryDTO categoryDTO = categoryData.getCategory(transactionRequest.getCategoryId());
        accountData.checkAccountExists(transactionRequest.getAccountId());

        // save transaction
        validateTransferInfo(categoryDTO, transactionRequest);
        transactionData.createTransaction(transactionRequest);

        // Deduct balance from an account
        BigDecimal amount = transactionRequest.getAmount().abs();

        if (CategoryType.TRANSFER.equals(categoryDTO.type())) {
            Long sourceAccountId = transactionRequest.getAccountId();
            Long targetAccountId = transactionRequest.getTargetAccountId();
            accountData.transferMoney(sourceAccountId, targetAccountId, amount);
            return;
        }

        BigDecimal finalAmount = CategoryType.EXPENSE.equals(categoryDTO.type()) ? amount.negate() : amount;
        accountData.updateAvailableBalance(transactionRequest.getAccountId(), finalAmount);
    }

    private void validateTransferInfo(CategoryDTO categoryDTO, TransactionDTO transactionRequest) {
        if (!CategoryType.TRANSFER.equals(categoryDTO.type())) {
            return;
        }

        Long accountId = transactionRequest.getAccountId();
        Long toAccountId = transactionRequest.getTargetAccountId();

        if (toAccountId == null || accountId.equals(toAccountId)) {
            logger.info("toAccountId is null for transaction type transfer");
            throw new BadRequestException(ErrorCode.INVALID_REQUEST_DATA);
        }

        List<AccountFlatView> accounts = accountData.retrieveAccountByIdList(List.of(accountId, toAccountId));
        if (accounts.size() != 2) {
            logger.info("Invalid account ID list. To account doesn't exist {}", toAccountId);
            throw new BadRequestException(ErrorCode.INVALID_REQUEST_DATA);
        }

        AccountFlatView fromAccount = accounts.getFirst();
        AccountFlatView toAccount = accounts.get(1);

        // Ensure the same currency for transfer
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            logger.info("Transfer failed: currency mismatch. from='{}', to='{}'", fromAccount.getCurrency(), toAccount.getCurrency());
            throw new ConflictException(ErrorCode.CURRENCY_TRANSFER_SHOULD_BE_THE_SAME);
        }

        // Check valid balance
        BigDecimal sourceBalance = fromAccount.getAmount();
        BigDecimal requestAmount = transactionRequest.getAmount();
        if (sourceBalance.compareTo(requestAmount) < 0) {
            throw new ConflictException(ErrorCode.SOURCE_ACCOUNT_BALANCE_NOT_ENOUGH_MONEY);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionPagination retrieveTransactions(RetrieveTransactionsParams params) {
        logger.info("Retrieving transactions with params: {}", params);
        Page<Transaction> transactionPage = transactionData.retrieveTransactions(params);

        List<Transaction> transactions = transactionPage.getContent();

        List<TransactionDTO> transactionDTOList = new ArrayList<>();
        for (Transaction transaction : transactions) {

            String transferInfo = "N/A";
            if (transaction.getType() == CategoryType.TRANSFER) {
                Account sourceAccount = transaction.getSourceAccount();
                Account targetAccount = transaction.getTargetAccount();
                transferInfo = String.format("%s -> %s", sourceAccount.getName(), targetAccount.getName());
            }

            TransactionDTO transactionDTO = TransactionDTO
                    .builder()
                    .id(transaction.getId())
                    .name(transaction.getName())
                    .amount(transaction.getAmount())
                    .transferInfo(transferInfo)
                    .date(transaction.getDate())
                    .accountName(transaction.getSourceAccount().getName())
                    .categoryName(transaction.getCategory().getIdentity().getName())
                    .build();

            transactionDTOList.add(transactionDTO);
        }

        TransactionPagination.Pagination pagination = new TransactionPagination.Pagination(
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
        );
        return new TransactionPagination(pagination, transactionDTOList);
    }
}
