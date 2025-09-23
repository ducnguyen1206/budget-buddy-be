package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.transaction.RetrieveTransactionsParams;
import com.budget.buddy.transaction.application.dto.transaction.TransactionDTO;
import com.budget.buddy.transaction.application.dto.transaction.TransactionFilterCriteria;
import com.budget.buddy.transaction.application.dto.transaction.TransactionPagination;
import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.budget.buddy.transaction.domain.enums.Direction;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.category.Category;
import com.budget.buddy.transaction.domain.model.transaction.Transaction;
import com.budget.buddy.transaction.domain.service.TransactionData;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.CategoryRepository;
import com.budget.buddy.transaction.infrastructure.repository.TransactionRepository;
import com.budget.buddy.transaction.infrastructure.repository.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionDataImpl implements TransactionData {
    private final TransactionRepository transactionRepository;
    private final TransactionUtils transactionUtils;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final TransactionSpecification transactionSpecification;
    private static final Logger logger = LogManager.getLogger(TransactionDataImpl.class);

    @Transactional
    @Override
    public void createTransaction(TransactionDTO transactionRequest) {
        Long userId = transactionUtils.getCurrentUserId();
        Account sourceAccount = getAccount(userId, transactionRequest.getAccountId());
        Category category = getCategory(transactionRequest.getCategoryId());

        logger.info("Creating transaction: userId='{}', amount='{}', categoryId='{}', sourceAccountId='{}'",
                userId, transactionRequest.getAmount(), category.getId(), sourceAccount.getId());

        Transaction sourceTransaction = buildTransaction(userId, sourceAccount, category, transactionRequest,
                CategoryType.INCOME.equals(category.getIdentity().getType()) ? Direction.IN : Direction.OUT);

        List<Transaction> transactions = new ArrayList<>(2);
        transactions.add(sourceTransaction);

        // For transfer category, also record a transaction entry for the target account
        if (CategoryType.TRANSFER.equals(category.getIdentity().getType())) {
            Account targetAccount = getAccount(userId, transactionRequest.getTargetAccountId());
            transactions.add(buildTransaction(userId, targetAccount, category, transactionRequest, Direction.IN));
            logger.info("Creating transfer mirror transaction: userId='{}', amount='{}', categoryId='{}', targetAccountId='{}'",
                    userId, transactionRequest.getAmount(), category.getId(), targetAccount.getId());
        }

        transactionRepository.saveAll(transactions);
    }

    private Transaction buildTransaction(Long userId, Account account, Category category, TransactionDTO dto, Direction direction) {
        BigDecimal amount = direction.equals(Direction.OUT) ? dto.getAmount().abs().negate() : dto.getAmount().abs();
        return new Transaction(
                userId,
                account,
                category,
                dto.getName(),
                amount,
                dto.getDate(),
                category.getIdentity().getType(),
                dto.getRemarks()
        );
    }

    @Transactional
    @Override
    public void deleteTransactionByAccountId(List<Long> accountIds) {
        List<Transaction> transactions = transactionRepository.findTransactionBySourceAccountIdIn(accountIds);
        transactionRepository.deleteAll(transactions);
    }

    @Transactional(readOnly = true)
    @Override
    public TransactionPagination retrieveTransactions(RetrieveTransactionsParams params, TransactionFilterCriteria filterCriteria) {
        int page = Optional.ofNullable(params.getPage()).orElse(0);
        int size = Optional.ofNullable(params.getSize()).orElse(20);

        Pageable pageable = PageRequest.of(page, size);

        String sort = filterCriteria.getSort();
        logger.info("Fetching transactions with page {} size {} sort {}", page, size, sort);

        Specification<Transaction> specification = transactionSpecification.buildSpecification(filterCriteria, sort);

        Page<Transaction> transactionPage = transactionRepository.findAll(specification, pageable);

        List<TransactionDTO> transactionDTOList = transactionPage.getContent()
                .stream()
                .map(this::toDto)
                .toList();

        TransactionPagination.Pagination pagination = new TransactionPagination.Pagination(
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
        );

        return new TransactionPagination(pagination, transactionDTOList);
    }

    private TransactionDTO toDto(Transaction transaction) {
        Account sourceAccount = transaction.getSourceAccount();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = transaction.getDate().format(formatter);

        return TransactionDTO.builder()
                .id(transaction.getId())
                .name(transaction.getName())
                .amount(transaction.getAmount())
                .remarks(transaction.getRemarks())
                .date(transaction.getDate())
                .formattedDate(formattedDate)
                .sourceAccountName(sourceAccount.getName())
                .categoryName(transaction.getCategory().getIdentity().getName())
                .currency(sourceAccount.getMoney().getCurrency())
                .categoryType(transaction.getCategory().getIdentity().getType())
                .accountId(transaction.getSourceAccount().getId())
                .sourceAccountType(transaction.getSourceAccount().getAccountTypeGroup().getName())
                .categoryId(transaction.getCategory().getId())
                .build();
    }

    private Account getAccount(Long userId, Long accountId) {
        return accountRepository.findAccountByUserIdAndAccountId(userId, accountId);
    }

    private Category getCategory(Long category) {
        return categoryRepository.findById(category)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
