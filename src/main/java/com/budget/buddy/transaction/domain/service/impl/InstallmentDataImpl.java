package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.installment.InstallmentDTO;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.installment.Installment;
import com.budget.buddy.transaction.domain.service.InstallmentData;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstallmentDataImpl implements InstallmentData {
    private static final Logger logger = LogManager.getLogger(InstallmentDataImpl.class);

    private final InstallmentRepository installmentRepository;
    private final AccountRepository accountRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public void create(InstallmentDTO request) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Creating installment for userId='{}' with name='{}' and accountId='{}'", userId, request.name(), request.accountId());

        Account account = validateAndGetOwnedAccount(userId, request.accountId());

        Installment installment = new Installment(userId, account, request.name(), request.totalAmount(), request.amountPaid(), request.dueDate());
        installmentRepository.save(installment);
    }

    @Override
    public void update(Long id, InstallmentDTO request) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Updating installment id='{}' for userId='{}'", id, userId);

        Installment installment = installmentRepository.findByIdAndUserIdOrderByIdAsc(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.INSTALLMENT_NOT_FOUND));

        if (!installment.getAccount().getId().equals(request.accountId())) {
            Account account = validateAndGetOwnedAccount(userId, request.accountId());
            installment.setAccount(account);
        }

        installment.setName(request.name());
        installment.setTotalAmount(request.totalAmount());
        installment.setAmountPaid(request.amountPaid());
        installment.setDueDate(request.dueDate());
        installmentRepository.save(installment);
    }

    @Override
    public void delete(Long id) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Deleting installment id='{}' for userId='{}'", id, userId);
        Installment installment = installmentRepository.findByIdAndUserIdOrderByIdAsc(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.INSTALLMENT_NOT_FOUND));
        installmentRepository.delete(installment);
    }

    @Override
    public void deleteAll(Collection<Long> ids) {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Deleting {} installments for userId='{}'", ids.size(), userId);
        installmentRepository.deleteAllByIdInAndUserId(ids, userId);
    }

    @Override
    public InstallmentDTO getById(Long id) {
        Long userId = transactionUtils.getCurrentUserId();
        Installment installment = installmentRepository.findByIdAndUserIdOrderByIdAsc(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.INSTALLMENT_NOT_FOUND));
        return toDTO(installment);
    }

    @Override
    public List<InstallmentDTO> getAll() {
        Long userId = transactionUtils.getCurrentUserId();
        logger.info("Retrieving all installments for userId='{}'", userId);
        List<Installment> installments = installmentRepository.findAllByUserIdOrderByDueDateAscIdAsc(userId);
        return installments.stream().map(this::toDTO).toList();
    }

    private Account validateAndGetOwnedAccount(Long userId, Long accountId) {
        if (!accountRepository.existsAccountBy(userId, accountId)) {
            throw new ConflictException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        return accountRepository.findAccountByUserIdAndAccountId(userId, accountId);
    }

    private InstallmentDTO toDTO(Installment i) {
        return new InstallmentDTO(
                i.getId(),
                i.getAccount().getId(),
                i.getAccount().getName(),
                i.getName(),
                i.getTotalAmount(),
                i.getAmountPaid(),
                i.getOutstandingAmount(),
                i.getDueDate(),
                i.getAccount().getCurrency().name(),
                i.getLastModifiedDate()
        );
    }
}
