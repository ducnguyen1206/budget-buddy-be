package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.installment.InstallmentDTO;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.installment.Installment;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.InstallmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstallmentDataImplTest {

    @Mock
    private InstallmentRepository installmentRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionUtils transactionUtils;

    @InjectMocks
    private InstallmentDataImpl installmentData;

    private static final Long USER_ID = 1L;

    private Account buildAccount(Long id, Currency currency, String name) {
        Account a = new Account();
        a.setId(id);
        a.setName(name);
        a.setCurrency(currency);
        return a;
    }

    private Installment buildInstallment(Long id, Long userId, Account account) {
        Installment i = new Installment(
                userId,
                account,
                "Car Loan",
                new BigDecimal("50000.00"),
                new BigDecimal("10000.00"),
                LocalDate.of(2026, 12, 31)
        );
        i.setId(id);
        return i;
    }

    @Test
    void create_success() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long accountId = 100L;
        Account account = buildAccount(accountId, Currency.SGD, "Main");
        when(accountRepository.existsAccountBy(USER_ID, accountId)).thenReturn(true);
        when(accountRepository.findAccountByUserIdAndAccountId(USER_ID, accountId)).thenReturn(account);

        InstallmentDTO req = new InstallmentDTO(null, accountId, null, "Car Loan",
                new BigDecimal("50000.00"), new BigDecimal("10000.00"), null,
                LocalDate.of(2026, 12, 31), null, null);

        installmentData.create(req);

        ArgumentCaptor<Installment> captor = ArgumentCaptor.forClass(Installment.class);
        verify(installmentRepository).save(captor.capture());
        Installment saved = captor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(accountId, saved.getAccount().getId());
        assertEquals("Car Loan", saved.getName());
        assertEquals(new BigDecimal("50000.00"), saved.getTotalAmount());
        assertEquals(new BigDecimal("10000.00"), saved.getAmountPaid());
        assertEquals(new BigDecimal("40000.00"), saved.getOutstandingAmount());
        assertEquals(LocalDate.of(2026, 12, 31), saved.getDueDate());
    }

    @Test
    void create_shouldThrow_whenAccountNotOwned() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long accountId = 100L;
        when(accountRepository.existsAccountBy(USER_ID, accountId)).thenReturn(false);

        InstallmentDTO req = new InstallmentDTO(null, accountId, null, "Car Loan",
                new BigDecimal("50000.00"), new BigDecimal("10000.00"), null,
                LocalDate.now(), null, null);

        assertThrows(ConflictException.class, () -> installmentData.create(req));
        verify(installmentRepository, never()).save(any());
    }

    @Test
    void update_success() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long accountId = 100L;
        Account account = buildAccount(accountId, Currency.SGD, "Main");
        Installment existing = buildInstallment(5L, USER_ID, account);

        when(installmentRepository.findByIdAndUserIdOrderByIdAsc(5L, USER_ID)).thenReturn(Optional.of(existing));

        InstallmentDTO req = new InstallmentDTO(null, accountId, null, "Updated Loan",
                new BigDecimal("60000.00"), new BigDecimal("20000.00"), null,
                LocalDate.of(2027, 6, 30), null, null);

        installmentData.update(5L, req);

        verify(installmentRepository).save(existing);
        assertEquals("Updated Loan", existing.getName());
        assertEquals(new BigDecimal("60000.00"), existing.getTotalAmount());
        assertEquals(new BigDecimal("20000.00"), existing.getAmountPaid());
        assertEquals(LocalDate.of(2027, 6, 30), existing.getDueDate());
    }

    @Test
    void update_shouldChangeAccount_whenAccountIdDiffers() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long oldAccountId = 100L;
        Long newAccountId = 200L;
        Account oldAccount = buildAccount(oldAccountId, Currency.SGD, "Old Account");
        Account newAccount = buildAccount(newAccountId, Currency.SGD, "New Account");
        Installment existing = buildInstallment(5L, USER_ID, oldAccount);

        when(installmentRepository.findByIdAndUserIdOrderByIdAsc(5L, USER_ID)).thenReturn(Optional.of(existing));
        when(accountRepository.existsAccountBy(USER_ID, newAccountId)).thenReturn(true);
        when(accountRepository.findAccountByUserIdAndAccountId(USER_ID, newAccountId)).thenReturn(newAccount);

        InstallmentDTO req = new InstallmentDTO(null, newAccountId, null, "Car Loan",
                new BigDecimal("50000.00"), new BigDecimal("10000.00"), null,
                LocalDate.of(2026, 12, 31), null, null);

        installmentData.update(5L, req);

        verify(installmentRepository).save(existing);
        assertEquals(newAccountId, existing.getAccount().getId());
    }

    @Test
    void update_shouldThrow_whenInstallmentNotFound() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(installmentRepository.findByIdAndUserIdOrderByIdAsc(10L, USER_ID)).thenReturn(Optional.empty());

        InstallmentDTO req = new InstallmentDTO(null, 100L, null, "Loan",
                new BigDecimal("50000.00"), new BigDecimal("10000.00"), null,
                LocalDate.now(), null, null);

        assertThrows(NotFoundException.class, () -> installmentData.update(10L, req));
        verify(installmentRepository, never()).save(any());
    }

    @Test
    void getById_success_mapsToDTO() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Account account = buildAccount(100L, Currency.SGD, "Main");
        Installment installment = buildInstallment(5L, USER_ID, account);
        when(installmentRepository.findByIdAndUserIdOrderByIdAsc(5L, USER_ID)).thenReturn(Optional.of(installment));

        var dto = installmentData.getById(5L);
        assertEquals(5L, dto.id());
        assertEquals(100L, dto.accountId());
        assertEquals("Main", dto.accountName());
        assertEquals("Car Loan", dto.name());
        assertEquals(new BigDecimal("50000.00"), dto.totalAmount());
        assertEquals(new BigDecimal("10000.00"), dto.amountPaid());
        assertEquals(new BigDecimal("40000.00"), dto.outstandingAmount());
        assertEquals(LocalDate.of(2026, 12, 31), dto.dueDate());
        assertEquals("SGD", dto.currency());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(installmentRepository.findByIdAndUserIdOrderByIdAsc(99L, USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> installmentData.getById(99L));
    }

    @Test
    void getAll_returnsAllInstallments() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Account account = buildAccount(100L, Currency.SGD, "Main");
        Installment installment = buildInstallment(5L, USER_ID, account);

        when(installmentRepository.findAllByUserIdOrderByDueDateAscIdAsc(USER_ID)).thenReturn(List.of(installment));

        var all = installmentData.getAll();
        assertEquals(1, all.size());
        assertEquals(5L, all.get(0).id());
        verify(installmentRepository).findAllByUserIdOrderByDueDateAscIdAsc(USER_ID);
    }

    @Test
    void delete_success() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Account account = buildAccount(100L, Currency.SGD, "Main");
        Installment installment = buildInstallment(5L, USER_ID, account);
        when(installmentRepository.findByIdAndUserIdOrderByIdAsc(5L, USER_ID)).thenReturn(Optional.of(installment));

        installmentData.delete(5L);

        verify(installmentRepository).delete(installment);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(installmentRepository.findByIdAndUserIdOrderByIdAsc(9L, USER_ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> installmentData.delete(9L));
        verify(installmentRepository, never()).delete(any());
    }

    @Test
    void deleteAll_delegatesToRepository() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        List<Long> ids = List.of(1L, 2L, 3L);
        installmentData.deleteAll(ids);
        verify(installmentRepository).deleteAllByIdInAndUserId(ids, USER_ID);
    }
}
