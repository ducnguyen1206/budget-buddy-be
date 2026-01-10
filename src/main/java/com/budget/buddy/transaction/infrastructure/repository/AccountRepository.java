package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.application.dto.account.AccountFlatView;
import com.budget.buddy.transaction.domain.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("""
            SELECT a.id AS id, a.name AS name, a.currency AS currency, g.name AS groupName, g.id AS groupId, a.savingAccount as savingAccount
            FROM Account a JOIN a.accountTypeGroup g
            WHERE g.userId = :userId
            ORDER BY g.name, a.name
            """)
    List<AccountFlatView> retrieveAllAccounts(Long userId);

    @Query("""
            SELECT a.id AS id, a.name AS name, a.currency AS currency, g.name AS groupName, g.id AS groupId, a.savingAccount as savingAccount
            FROM Account a JOIN a.accountTypeGroup g
            WHERE g.userId = :userId AND a.savingAccount = :savingAccount
            ORDER BY g.name, a.name
            """)
    List<AccountFlatView> retrieveAllAccountsBySavingAccount(Long userId, Boolean savingAccount);

    @Query("""
            SELECT a.id AS id, COALESCE(SUM(t.amount), 0) AS amount, a.savingAccount
            FROM Account a JOIN a.accountTypeGroup g
                        LEFT JOIN Transaction t ON a.id = t.sourceAccount.id
            WHERE g.userId = :userId AND a.id IN (:accountIds)
            GROUP BY a.id
            ORDER BY a.id
            """)
    List<AccountFlatView> retrieveAccountBalance(List<Long> accountIds, Long userId);

    @Query("SELECT a.id AS id, a.name AS name, a.currency AS currency, g.name AS groupName, g.id AS groupId, a.savingAccount " +
            "FROM Account a JOIN a.accountTypeGroup g " +
            "WHERE g.userId = :userId AND a.id = :accountId " +
            "ORDER BY g.name, a.name")
    AccountFlatView retrieveByAccountId(Long userId, Long accountId);


    @Query("SELECT (COUNT(a) > 0) " +
            "FROM Account a JOIN a.accountTypeGroup g " +
            "WHERE g.userId = :userId AND a.id = :accountId")
    boolean existsAccountBy(Long userId, Long accountId);

    @Query("SELECT a " +
            "FROM Account a JOIN a.accountTypeGroup g " +
            "WHERE g.userId = :userId AND a.id = :accountId " +
            "ORDER BY g.name, a.name")
    Account findAccountByUserIdAndAccountId(Long userId, Long accountId);

    @Query("SELECT a " +
            "FROM Account a JOIN a.accountTypeGroup g " +
            "WHERE g.userId = :userId AND a.id IN (:accountIds) " +
            "ORDER BY g.name, a.name")
    List<Account> findAccountByUserIdAndAccountIdIn(Long userId, List<Long> accountIds);

    @Query("SELECT a.id AS id, a.name AS name, a.currency AS currency, g.name AS groupName, g.id AS groupId , a.savingAccount " +
            "FROM Account a JOIN a.accountTypeGroup g " +
            "WHERE g.userId = :userId AND a.id IN (:accountId) " +
            "ORDER BY g.name, a.name")
    List<AccountFlatView> retrieveByAccountIdIn(Long userId, List<Long> accountId);
}
