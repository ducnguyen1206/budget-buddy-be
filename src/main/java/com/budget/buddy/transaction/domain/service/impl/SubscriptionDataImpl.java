package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.subscription.SubscriptionDTO;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.subscription.Subscription;
import com.budget.buddy.transaction.domain.service.SubscriptionData;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionDataImpl implements SubscriptionData {
    private static final Logger logger = LogManager.getLogger(SubscriptionDataImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final AccountRepository accountRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public void create(SubscriptionDTO request) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Creating subscription for userId='{}' with name='{}' and accountId='{}'", userId, request.name(), request.accountId());

        Account account = validateAndGetOwnedAccount(userId, request.accountId());

        Subscription subscription = new Subscription(userId, account, request.name(), request.amount(), request.payDay());
        subscriptionRepository.save(subscription);
    }

    @Override
    public SubscriptionDTO update(Long id, SubscriptionDTO request) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Updating subscription id='{}' for userId='{}'", id, userId);

        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getAccount().getId().equals(request.accountId())) {
            Account account = validateAndGetOwnedAccount(userId, request.accountId());
            subscription.setAccount(account);
        }

        subscription.setName(request.name());
        subscription.setAmount(request.amount());
        subscription.setPayDay(request.payDay());
        Subscription saved = subscriptionRepository.save(subscription);
        return toDTO(saved);
    }

    @Override
    public void delete(Long id) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Deleting subscription id='{}' for userId='{}'", id, userId);
        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        subscriptionRepository.delete(subscription);
    }

    @Override
    public SubscriptionDTO getById(Long id) {
        Long userId = transactionUtils.getCurrentUserId();
        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        return toDTO(subscription);
    }

    @Override
    public List<SubscriptionDTO> getAll() {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Retrieving all subscriptions for userId='{}'", userId);
        List<Subscription> subscriptions = subscriptionRepository.findAllByUserIdOrderByPayDayAscIdAsc(userId);
        return subscriptions.stream().map(this::toDTO).toList();
    }

    private Account validateAndGetOwnedAccount(Long userId, Long accountId) {
        if (!accountRepository.existsAccountBy(userId, accountId)) {
            throw new ConflictException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        return accountRepository.findAccountByUserIdAndAccountId(userId, accountId);
    }

    private SubscriptionDTO toDTO(Subscription s) {
        return new SubscriptionDTO(
                s.getId(),
                s.getAccount().getId(),
                s.getAccount().getName(),
                s.getName(),
                s.getAmount(),
                s.getPayDay(),
                s.getAccount().getCurrency().name(),
                s.getLastModifiedDate()
        );
    }
}
