package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.application.dto.account.AccountFlatView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("select a.id as id, a.name as name, a.money.amount as amount, a.money.currency as currency, g.name as groupName, g.id as groupId " +
            "from Account a join a.accountTypeGroup g " +
            "where g.userId = :userId " +
            "order by g.name, a.name")
    List<AccountFlatView> retrieveAllAccounts(Long userId);

    @Query("select a.id as id, a.name as name, a.money.amount as amount, a.money.currency as currency, g.name as groupName, g.id as groupId " +
            "from Account a join a.accountTypeGroup g " +
            "where g.userId = :userId and a.id = :accountId " +
            "order by g.name, a.name")
    AccountFlatView retrieveByAccountId(Long userId, Long accountId);


    @Query("select (count(a) > 0) " +
            "from Account a join a.accountTypeGroup g " +
            "where g.userId = :userId and a.id = :accountId")
    boolean existsAccountBy(Long userId, Long accountId);

    @Query("select a " +
            "from Account a join a.accountTypeGroup g " +
            "where g.userId = :userId and a.id = :accountId " +
            "order by g.name, a.name")
    Account findAccountByUserIdAndAccountId(Long userId, Long accountId);

    @Query("select a.id as id, a.name as name, a.money.amount as amount, a.money.currency as currency, g.name as groupName, g.id as groupId " +
            "from Account a join a.accountTypeGroup g " +
            "where g.userId = :userId and a.id IN (:accountId) " +
            "order by g.name, a.name")
    List<AccountFlatView> retrieveByAccountIdIn(Long userId, List<Long> accountId);

    List<Account> findByIdIn(List<Long> accountIds);
}
