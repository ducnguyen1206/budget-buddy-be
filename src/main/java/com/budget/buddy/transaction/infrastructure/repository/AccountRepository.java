package com.budget.buddy.transaction.infrastructure.repository;

import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.infrastructure.view.AccountFlatView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("select a.id as id, a.name as name, a.money.amount as amount, a.money.currency as currency, g.name as groupName, g.id as groupId " +
            "from Account a join a.accountTypeGroup g " +
            "where g.userId = :userId " +
            "order by g.name, a.name")
    List<AccountFlatView> retreiveAllAccounts(Long userId);

    @Query("select a.id as id, a.name as name, a.money.amount as amount, a.money.currency as currency, g.name as groupName, g.id as groupId " +
            "from Account a join a.accountTypeGroup g " +
            "where g.userId = :userId and a.id = :accountId " +
            "order by g.name, a.name limit 1")
    AccountFlatView retrieveByAccountId(Long userId, Long accountId);


    @Query("select (count(a) > 0) " +
            "from Account a join a.accountTypeGroup g " +
            "where g.userId = :userId and a.id = :accountId")
    boolean existsAccountBy(Long userId, Long accountId);
}
