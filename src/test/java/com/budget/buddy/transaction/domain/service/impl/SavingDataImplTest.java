package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.BadRequestException;
import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.saving.SavingDTO;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.saving.Saving;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.SavingRepository;
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
class SavingDataImplTest {

    @Mock
    private SavingRepository savingRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionUtils transactionUtils;

    @InjectMocks
    private SavingDataImpl savingData;

    private static final Long USER_ID = 1L;

    private Account buildAccount(Long id, Currency currency, String name) {
        Account a = new Account();
        a.setId(id);
        a.setName(name);
        a.setCurrency(currency);
        return a;
    }

    private Saving buildSaving(Long id, Long userId, Account account) {
        Saving s = new Saving(
                userId,
                account,
                "Vacation",
                new com.budget.buddy.transaction.domain.vo.MoneyVO(new BigDecimal("200.00"), Currency.SGD),
                LocalDate.of(2026, 1, 15),
                "Notes"
        );
        s.setId(id);
        return s;
    }

    @Test
    void create_success() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long accountId = 100L;
        Account account = buildAccount(accountId, Currency.SGD, "Main");
        when(accountRepository.existsAccountBy(USER_ID, accountId)).thenReturn(true);
        when(accountRepository.findAccountByUserIdAndAccountId(USER_ID, accountId)).thenReturn(account);

        SavingDTO req = new SavingDTO(null, accountId, null, "Trip", new BigDecimal("500.00"), "SGD", LocalDate.of(2026, 1, 15), "save", null);

        savingData.create(req);

        ArgumentCaptor<Saving> captor = ArgumentCaptor.forClass(Saving.class);
        verify(savingRepository).save(captor.capture());
        Saving saved = captor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(accountId, saved.getAccount().getId());
        assertEquals("Trip", saved.getName());
        assertEquals(new BigDecimal("500.00"), saved.getMoney().getAmount());
        assertEquals("SGD", saved.getMoney().getCurrency());
        assertEquals(LocalDate.of(2026, 1, 15), saved.getDate());
        assertEquals("save", saved.getNotes());
    }

    @Test
    void create_shouldThrow_whenCurrencyMismatch() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long accountId = 100L;
        Account account = buildAccount(accountId, Currency.VND, "Main");
        when(accountRepository.existsAccountBy(USER_ID, accountId)).thenReturn(true);
        when(accountRepository.findAccountByUserIdAndAccountId(USER_ID, accountId)).thenReturn(account);

        SavingDTO req = new SavingDTO(null, accountId, null, "Trip", new BigDecimal("500.00"), "SGD", LocalDate.now(), null, null);

        assertThrows(BadRequestException.class, () -> savingData.create(req));
        verify(savingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenAccountNotOwned() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long accountId = 100L;
        when(accountRepository.existsAccountBy(USER_ID, accountId)).thenReturn(false);

        SavingDTO req = new SavingDTO(null, accountId, null, "Trip", new BigDecimal("500.00"), "SGD", LocalDate.now(), null, null);

        assertThrows(ConflictException.class, () -> savingData.create(req));
        verify(savingRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenSavingNotFound() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(savingRepository.findByIdAndUserIdOrderByIdAsc(10L, USER_ID)).thenReturn(Optional.empty());

        SavingDTO req = new SavingDTO(null, 100L, null, "Trip", new BigDecimal("500.00"), "SGD", LocalDate.now(), null, null);

        assertThrows(NotFoundException.class, () -> savingData.update(10L, req));
        verify(savingRepository, never()).save(any());
    }

    @Test
    void getById_success_mapsToDTO() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Account account = buildAccount(100L, Currency.SGD, "Main");
        Saving saving = buildSaving(5L, USER_ID, account);
        when(savingRepository.findByIdAndUserIdOrderByIdAsc(5L, USER_ID)).thenReturn(Optional.of(saving));

        var dto = savingData.getById(5L);
        assertEquals(5L, dto.id());
        assertEquals(100L, dto.accountId());
        assertEquals("Main", dto.accountName());
        assertEquals("Vacation", dto.name());
        assertEquals(new BigDecimal("200.00"), dto.amount());
        assertEquals("SGD", dto.currency());
        assertEquals(LocalDate.of(2026, 1, 15), dto.date());
        assertEquals("Notes", dto.notes());
    }

    @Test
    void getAll_withAndWithoutCurrency() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Account account = buildAccount(100L, Currency.SGD, "Main");
        Saving saving = buildSaving(5L, USER_ID, account);

        when(savingRepository.findAllByUserIdOrderByDateAscIdAsc(USER_ID)).thenReturn(List.of(saving));
        when(savingRepository.findAllByUserIdAndMoney_CurrencyOrderByDateAscIdAsc(USER_ID, "SGD")).thenReturn(List.of(saving));

        var all = savingData.getAll(null);
        assertEquals(1, all.size());
        verify(savingRepository).findAllByUserIdOrderByDateAscIdAsc(USER_ID);

        var filtered = savingData.getAll("SGD");
        assertEquals(1, filtered.size());
        verify(savingRepository).findAllByUserIdAndMoney_CurrencyOrderByDateAscIdAsc(USER_ID, "SGD");
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(savingRepository.findByIdAndUserIdOrderByIdAsc(9L, USER_ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> savingData.delete(9L));
        verify(savingRepository, never()).delete(any());
    }

    @Test
    void deleteAll_delegatesToRepository() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        List<Long> ids = List.of(1L, 2L, 3L);
        savingData.deleteAll(ids);
        verify(savingRepository).deleteAllByIdInAndUserId(ids, USER_ID);
    }
}
