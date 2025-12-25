package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ConflictException;
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
        if (sourceAccount == null) {
            throw new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        Category category = getCategory(userId, transactionRequest.getCategoryId());
        CategoryType categoryType = transactionRequest.getCategoryType();
        Direction direction =  CategoryType.INCOME.equals(categoryType) ? Direction.IN : Direction.OUT;

        logger.info("Creating transaction: userId='{}', amount='{}', categoryId='{}', sourceAccountId='{}'",
                userId, transactionRequest.getAmount(), category.getId(), sourceAccount.getId());

        Transaction sourceTransaction = buildTransaction(userId, sourceAccount, category, transactionRequest,
                direction, categoryType);

        List<Transaction> transactions = new ArrayList<>(2);
        transactions.add(sourceTransaction);

        // For transfer category, also record a transaction entry for the target account
        if (CategoryType.TRANSFER.equals(categoryType)) {
            Account targetAccount = getAccount(userId, transactionRequest.getTargetAccountId());
            transactions.add(buildTransaction(userId, targetAccount, category, transactionRequest, Direction.IN, categoryType));
            logger.info("Creating transfer mirror transaction: userId='{}', amount='{}', categoryId='{}', targetAccountId='{}'",
                    userId, transactionRequest.getAmount(), category.getId(), targetAccount.getId());
        }

        transactionRepository.saveAll(transactions);
    }

    private Transaction buildTransaction(Long userId, Account account, Category category, TransactionDTO dto, Direction direction, CategoryType categoryType) {
        BigDecimal amount = direction.equals(Direction.OUT) ? dto.getAmount().abs().negate() : dto.getAmount().abs();
        return new Transaction(
                userId,
                account,
                category,
                dto.getName(),
                amount,
                dto.getDate(),
                categoryType,
                dto.getRemarks()
        );
    }

    @Transactional
    @Override
    public void deleteTransactionByAccountId(List<Long> accountIds) {
        Long userId = transactionUtils.getCurrentUserId();
        List<Transaction> transactions = transactionRepository.findTransactionBySourceAccountIdInAndUserId(accountIds, userId);
        transactionRepository.deleteAll(transactions);
    }

    @Transactional
    @Override
    public void deleteTransactionByCategoryId(Long categoryId) {
        Long userId = transactionUtils.getCurrentUserId();
        List<Transaction> transactions = transactionRepository.findTransactionByCategoryIdAndUserId(categoryId, userId);
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

    @Transactional
    @Override
    public void updateTransaction(Long transactionId, TransactionDTO transactionRequest) {
        Long userId = transactionUtils.getCurrentUserId();

        logger.info("Updating transaction: transactionId='{}', userId='{}'", transactionId, userId);

        Transaction existing = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ConflictException(ErrorCode.TRANSACTION_IS_NOT_BELONG_TO_USER));

        logger.info("Found existing transaction: id='{}', oldAmount='{}', oldCategoryId='{}', oldAccountId='{}'",
                existing.getId(), existing.getAmount(), existing.getCategory().getId(), existing.getSourceAccount().getId());

        Account sourceAccount = getAccount(userId, transactionRequest.getAccountId());
        Category category = getCategory(userId, transactionRequest.getCategoryId());

        logger.info("Fetched account and category: accountId='{}', categoryId='{}'",
                sourceAccount.getId(), category.getId());

        CategoryType categoryType = transactionRequest.getCategoryType();
        Direction direction = CategoryType.INCOME.equals(categoryType) ? Direction.IN : Direction.OUT;

        BigDecimal signedAmount = direction.equals(Direction.OUT)
                ? transactionRequest.getAmount().abs().negate()
                : transactionRequest.getAmount().abs();

        logger.info("Calculated update values: categoryType='{}', direction='{}', newAmount='{}', signedAmount='{}'",
                categoryType, direction, transactionRequest.getAmount(), signedAmount);

        existing.setSourceAccount(sourceAccount);
        existing.setCategory(category);
        existing.setName(transactionRequest.getName());
        existing.setAmount(signedAmount);
        existing.setDate(transactionRequest.getDate());
        existing.setType(categoryType);
        existing.setRemarks(transactionRequest.getRemarks());

        logger.info("Updated transaction fields: name='{}', date='{}', remarks='{}'",
                transactionRequest.getName(), transactionRequest.getDate(), transactionRequest.getRemarks());

        transactionRepository.save(existing);

        logger.info("Successfully saved updated transaction: transactionId='{}', userId='{}'", transactionId, userId);
    }

    @Transactional
    @Override
    public void deleteTransaction(Long transactionId) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Deleting transaction: transactionId='{}', userId='{}'", transactionId, userId);

        Transaction existing = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (CategoryType.TRANSFER.equals(existing.getType())) {
            logger.info("Deletion rejected for transfer transaction: id='{}'", transactionId);
            throw new BadRequestException(ErrorCode.INVALID_REQUEST_DATA);
        }

        transactionRepository.delete(existing);
        logger.info("Successfully deleted transaction: transactionId='{}', userId='{}'", transactionId, userId);
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
                .currency(sourceAccount.getCurrency().name())
                .categoryType(transaction.getType())
                .accountId(transaction.getSourceAccount().getId())
                .sourceAccountType(transaction.getSourceAccount().getAccountTypeGroup().getName())
                .categoryId(transaction.getCategory().getId())
                .build();
    }

    private Account getAccount(Long userId, Long accountId) {
        return accountRepository.findAccountByUserIdAndAccountId(userId, accountId);
    }

    private Category getCategory(Long userId, Long category) {
        return categoryRepository.findBydId(category, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
