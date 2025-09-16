package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.transaction.RetrieveTransactionsParams;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.domain.enums.CategoryType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionDataImpl implements TransactionData {
    private final TransactionRepository transactionRepository;
    private final TransactionUtils transactionUtils;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private static final Logger logger = LogManager.getLogger(TransactionDataImpl.class);

    @Transactional
    @Override
    public void createTransaction(TransactionDTO transactionRequest) {
        Long userId = transactionUtils.getCurrentUserId();
        Account account = getAccount(userId, transactionRequest.getAccountId());
        Category category = getCategory(transactionRequest.getCategoryId());

        logger.info("Creating transaction: amount='{}', category='{}', account='{}'",
                userId, account.getId(), category.getId());

        Account targetAccount = null;
        if (CategoryType.TRANSFER.equals(category.getIdentity().getType())) {
            targetAccount = getAccount(userId, transactionRequest.getTargetAccountId());
        }

        Transaction transaction = new Transaction(userId, account, category,
                transactionRequest.getName(), transactionRequest.getAmount(), transactionRequest.getDate(),
                category.getIdentity().getType(),
                targetAccount);
        transaction = transactionRepository.save(transaction);
        logger.info("saved transaction with ID: {}", transaction.getId());
    }

    @Transactional
    @Override
    public void deleteTransactionByAccountId(List<Long> accountIds) {
        List<Transaction> transactions = transactionRepository.findTransactionBySourceAccountIdIn(accountIds);
        transactionRepository.deleteAll(transactions);
    }

    @Override
    public Page<Transaction> retrieveTransactions(RetrieveTransactionsParams params) {
        int page = Optional.ofNullable(params.getPage()).orElse(0);
        int size = Optional.ofNullable(params.getSize()).orElse(20);
        String sortBy = Objects.requireNonNullElse(params.getSortBy(), "id");
        Sort.Direction direction = Objects.requireNonNullElse(params.getDirection(), Sort.Direction.ASC);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        logger.info("Fetching transactions with page {} size {} sort by {} direction {}", page, size, sortBy, direction);

        return transactionRepository.findAll(pageable);
    }

    private Account getAccount(Long userId, Long accountId) {
        return accountRepository.findAccountByUserIdAndAccountId(userId, accountId);
    }

    private Category getCategory(Long category) {
        return categoryRepository.findById(category)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
