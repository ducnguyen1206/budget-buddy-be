package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ConflictException;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.subscription.SubscriptionDTO;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.subscription.Subscription;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionDataImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionUtils transactionUtils;

    @InjectMocks
    private SubscriptionDataImpl subscriptionData;

    private static final Long USER_ID = 1L;

    private Account buildAccount(Long id, Currency currency, String name) {
        Account a = new Account();
        a.setId(id);
        a.setName(name);
        a.setCurrency(currency);
        return a;
    }

    private Subscription buildSubscription(Long id, Long userId, Account account) {
        Subscription s = new Subscription(
                userId,
                account,
                "Netflix",
                new BigDecimal("15.99"),
                15
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

        SubscriptionDTO req = new SubscriptionDTO(null, accountId, null, "Netflix",
                new BigDecimal("15.99"), 15, null, null);

        subscriptionData.create(req);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        Subscription saved = captor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(accountId, saved.getAccount().getId());
        assertEquals("Netflix", saved.getName());
        assertEquals(new BigDecimal("15.99"), saved.getAmount());
        assertEquals(15, saved.getPayDay());
    }

    @Test
    void create_shouldThrow_whenAccountNotOwned() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long accountId = 100L;
        when(accountRepository.existsAccountBy(USER_ID, accountId)).thenReturn(false);

        SubscriptionDTO req = new SubscriptionDTO(null, accountId, null, "Netflix",
                new BigDecimal("15.99"), 15, null, null);

        assertThrows(ConflictException.class, () -> subscriptionData.create(req));
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void update_success() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long accountId = 100L;
        Account account = buildAccount(accountId, Currency.SGD, "Main");
        Subscription existing = buildSubscription(5L, USER_ID, account);

        when(subscriptionRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(existing);

        SubscriptionDTO req = new SubscriptionDTO(null, accountId, null, "Spotify",
                new BigDecimal("9.99"), 20, null, null);

        SubscriptionDTO result = subscriptionData.update(5L, req);

        verify(subscriptionRepository).save(existing);
        assertEquals("Spotify", existing.getName());
        assertEquals(new BigDecimal("9.99"), existing.getAmount());
        assertEquals(20, existing.getPayDay());
        assertNotNull(result);
        assertEquals(5L, result.id());
    }

    @Test
    void update_shouldChangeAccount_whenAccountIdDiffers() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long oldAccountId = 100L;
        Long newAccountId = 200L;
        Account oldAccount = buildAccount(oldAccountId, Currency.SGD, "Old Account");
        Account newAccount = buildAccount(newAccountId, Currency.SGD, "New Account");
        Subscription existing = buildSubscription(5L, USER_ID, oldAccount);

        when(subscriptionRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(existing));
        when(accountRepository.existsAccountBy(USER_ID, newAccountId)).thenReturn(true);
        when(accountRepository.findAccountByUserIdAndAccountId(USER_ID, newAccountId)).thenReturn(newAccount);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(existing);

        SubscriptionDTO req = new SubscriptionDTO(null, newAccountId, null, "Netflix",
                new BigDecimal("15.99"), 15, null, null);

        subscriptionData.update(5L, req);

        verify(subscriptionRepository).save(existing);
        assertEquals(newAccountId, existing.getAccount().getId());
    }

    @Test
    void update_shouldThrow_whenSubscriptionNotFound() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(subscriptionRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(Optional.empty());

        SubscriptionDTO req = new SubscriptionDTO(null, 100L, null, "Netflix",
                new BigDecimal("15.99"), 15, null, null);

        assertThrows(NotFoundException.class, () -> subscriptionData.update(10L, req));
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenNewAccountNotOwned() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Long oldAccountId = 100L;
        Long newAccountId = 200L;
        Account oldAccount = buildAccount(oldAccountId, Currency.SGD, "Old Account");
        Subscription existing = buildSubscription(5L, USER_ID, oldAccount);

        when(subscriptionRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(existing));
        when(accountRepository.existsAccountBy(USER_ID, newAccountId)).thenReturn(false);

        SubscriptionDTO req = new SubscriptionDTO(null, newAccountId, null, "Netflix",
                new BigDecimal("15.99"), 15, null, null);

        assertThrows(ConflictException.class, () -> subscriptionData.update(5L, req));
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void getById_success_mapsToDTO() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Account account = buildAccount(100L, Currency.SGD, "Main");
        Subscription subscription = buildSubscription(5L, USER_ID, account);
        when(subscriptionRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(subscription));

        var dto = subscriptionData.getById(5L);

        assertEquals(5L, dto.id());
        assertEquals(100L, dto.accountId());
        assertEquals("Main", dto.accountName());
        assertEquals("Netflix", dto.name());
        assertEquals(new BigDecimal("15.99"), dto.amount());
        assertEquals(15, dto.payDay());
        assertEquals("SGD", dto.currency());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(subscriptionRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> subscriptionData.getById(99L));
    }

    @Test
    void getAll_returnsAllSubscriptions() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Account account = buildAccount(100L, Currency.SGD, "Main");
        Subscription subscription = buildSubscription(5L, USER_ID, account);

        when(subscriptionRepository.findAllByUserIdOrderByPayDayAscIdAsc(USER_ID)).thenReturn(List.of(subscription));

        var all = subscriptionData.getAll();

        assertEquals(1, all.size());
        assertEquals(5L, all.get(0).id());
        assertEquals("Netflix", all.get(0).name());
        verify(subscriptionRepository).findAllByUserIdOrderByPayDayAscIdAsc(USER_ID);
    }

    @Test
    void getAll_returnsEmptyList_whenNoSubscriptions() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(subscriptionRepository.findAllByUserIdOrderByPayDayAscIdAsc(USER_ID)).thenReturn(List.of());

        var all = subscriptionData.getAll();

        assertTrue(all.isEmpty());
        verify(subscriptionRepository).findAllByUserIdOrderByPayDayAscIdAsc(USER_ID);
    }

    @Test
    void delete_success() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        Account account = buildAccount(100L, Currency.SGD, "Main");
        Subscription subscription = buildSubscription(5L, USER_ID, account);
        when(subscriptionRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(subscription));

        subscriptionData.delete(5L);

        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(transactionUtils.getCurrentUserId()).thenReturn(USER_ID);
        when(subscriptionRepository.findByIdAndUserId(9L, USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> subscriptionData.delete(9L));
        verify(subscriptionRepository, never()).delete(any());
    }
}
