package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.saving.SavingDTO;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.saving.Saving;
import com.budget.buddy.transaction.domain.service.SavingData;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.domain.vo.MoneyVO;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.SavingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingDataImpl implements SavingData {
    private static final Logger logger = LogManager.getLogger(SavingDataImpl.class);

    private final SavingRepository savingRepository;
    private final AccountRepository accountRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public void create(SavingDTO request) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Creating saving for userId='{}' with name='{}' and accountId='{}'", userId, request.name(), request.accountId());

        Account account = validateAndGetOwnedAccount(userId, request.accountId());
        if (!account.getCurrency().equals(Currency.valueOf(request.currency()))) {
            throw new BadRequestException(ErrorCode.INVALID_REQUEST_DATA);
        }

        MoneyVO money = new MoneyVO(request.amount(), Currency.valueOf(request.currency()));
        Saving saving = new Saving(userId, account, request.name(), money, request.date(), request.notes());
        savingRepository.save(saving);
    }

    @Override
    public void update(Long id, SavingDTO request) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Updating saving id='{}' for userId='{}'", id, userId);

        Saving saving = savingRepository.findByIdAndUserIdOrderByIdAsc(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SAVING_NOT_FOUND));

        if (!saving.getAccount().getId().equals(request.accountId())) {
            Account account = validateAndGetOwnedAccount(userId, request.accountId());
            saving.setAccount(account);
        }

        saving.setName(request.name());
        saving.setMoney(new MoneyVO(request.amount(), Currency.valueOf(request.currency())));
        saving.setDate(request.date());
        saving.setNotes(request.notes());
        savingRepository.save(saving);
    }

    @Override
    public void delete(Long id) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Deleting saving id='{}' for userId='{}'", id, userId);
        Saving saving = savingRepository.findByIdAndUserIdOrderByIdAsc(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SAVING_NOT_FOUND));
        savingRepository.delete(saving);
    }

    @Override
    public void deleteAll(Collection<Long> ids) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Deleting {} savings for userId='{}'", ids.size(), userId);
        savingRepository.deleteAllByIdInAndUserId(ids, userId);
    }

    @Override
    public SavingDTO getById(Long id) {
        Long userId = transactionUtils.getCurrentUserId();
        Saving saving = savingRepository.findByIdAndUserIdOrderByIdAsc(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SAVING_NOT_FOUND));
        return toDTO(saving);
    }

    @Override
    public List<SavingDTO> getAll(String currency) {
        Long userId = transactionUtils.getCurrentUserId();
        List<Saving> savings;
        if (currency != null) {
            logger.info("Retrieving savings for userId='{}' and currency='{}'", userId, currency);
            savings = savingRepository.findAllByUserIdAndMoney_CurrencyOrderByDateAscIdAsc(userId, currency);
        } else {
            logger.info("Retrieving all savings for userId='{}'", userId);
            savings = savingRepository.findAllByUserIdOrderByDateAscIdAsc(userId);
        }
        return savings.stream().map(this::toDTO).toList();
    }

    private Account validateAndGetOwnedAccount(Long userId, Long accountId) {
        if (!accountRepository.existsAccountBy(userId, accountId)) {
            throw new ConflictException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        return accountRepository.findAccountByUserIdAndAccountId(userId, accountId);
    }

    private SavingDTO toDTO(Saving s) {
        return new SavingDTO(
                s.getId(),
                s.getAccount().getId(),
                s.getAccount().getName(),
                s.getName(),
                s.getMoney().getAmount(),
                s.getMoney().getCurrency(),
                s.getDate(),
                s.getNotes(),
                s.getLastModifiedDate()
        );
    }
}
