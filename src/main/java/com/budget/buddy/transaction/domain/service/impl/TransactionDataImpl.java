package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.application.mapper.TransactionMapper;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.category.Category;
import com.budget.buddy.transaction.domain.model.transaction.Transaction;
import com.budget.buddy.transaction.domain.service.TransactionData;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.CategoryRepository;
import com.budget.buddy.transaction.infrastructure.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionDataImpl implements TransactionData {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionUtils transactionUtils;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private static final Logger logger = LogManager.getLogger(TransactionDataImpl.class);

    @Override
    public void createTransaction(TransactionDTO transactionRequest) {
        Long userId = transactionUtils.getCurrentUserId();
        Account account = getAccount(userId, transactionRequest.getAccountId());
        Category category = getCategory(transactionRequest.getCategoryId());

        logger.info("Creating transaction: amount='{}', category='{}', account='{}'",
                userId, account.getId(), category.getId());

        Transaction transaction = transactionMapper.toTransaction(transactionRequest);
        transaction.setUserId(userId);
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setType(category.getIdentity().getType());
        transaction.setTransferInfo(transactionRequest.getTransferInfo());

        transaction = transactionRepository.save(transaction);
        logger.info("saved transaction with ID: {}", transaction.getId());
    }

    private Account getAccount(Long userId, Long accountId) {
        return accountRepository.findAccountByUserIdAndAccountId(userId, accountId);
    }

    private Category getCategory(Long category) {
        return categoryRepository.findById(category)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
