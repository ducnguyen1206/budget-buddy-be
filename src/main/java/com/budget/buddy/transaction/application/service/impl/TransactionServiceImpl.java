package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.application.mapper.TransactionMapper;
import com.budget.buddy.transaction.application.service.TransactionService;
import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.budget.buddy.transaction.domain.service.AccountData;
import com.budget.buddy.transaction.domain.service.CategoryData;
import com.budget.buddy.transaction.domain.service.TransactionData;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.infrastructure.view.AccountFlatView;
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
    private final CategoryData categoryData;
    private final AccountData accountData;
    private final TransactionMapper transactionMapper;
    private final TransactionUtils transactionUtils;
    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);

    @Override
    public void createTransaction(TransactionDTO transactionRequest) {
        CategoryDTO categoryDTO = categoryData.getCategory(transactionRequest.getCategoryId());
        accountData.checkAccountExists(transactionRequest.getAccountId());

        // save transaction
        String transferInfo = generateTransferInfo(categoryDTO, transactionRequest);
        transactionRequest.setTransferInfo(transferInfo);

        transactionData.createTransaction(transactionRequest);

        // Deduct balance from an account
        // TODO handle update available balance for transaction transfer
        BigDecimal amount = transactionRequest.getAmount().abs();
        BigDecimal finalAmount = CategoryType.EXPENSE.equals(categoryDTO.type()) ? amount.negate() : amount;
        accountData.updateAvailableBalance(transactionRequest.getAccountId(), finalAmount);
    }

    private String generateTransferInfo(CategoryDTO categoryDTO, TransactionDTO transactionRequest) {
        if (!CategoryType.TRANSFER.equals(categoryDTO.type())) {
            return "N/A";
        }

        Long accountId = transactionRequest.getAccountId();
        Long toAccountId = transactionRequest.getToAccountId();

        if (toAccountId == null || accountId.equals(toAccountId)) {
            logger.info("toAccountId is null for transaction type transfer");
            throw new BadRequestException(ErrorCode.INVALID_REQUEST_DATA);
        }

        List<AccountFlatView> accounts = accountData.retrieveAccountByIdList(List.of(accountId, toAccountId));
        if (accounts.size() != 2) {
            logger.info("Invalid account ID list. To account doesn't exist {}", toAccountId);
            throw new BadRequestException(ErrorCode.INVALID_REQUEST_DATA);
        }

        Map<Long, String> accountNameMap =
                accounts.stream()
                        .collect(Collectors.toMap(AccountFlatView::getId, AccountFlatView::getName));
        String transferInfo = String.format("%s -> %s", accountNameMap.get(accountId), accountNameMap.get(toAccountId));
        logger.info("Transfer info: {}", transferInfo);

        return transferInfo;
    }
}
