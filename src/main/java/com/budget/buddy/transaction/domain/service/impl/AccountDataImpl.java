package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.account.AccountTypeGroup;
import com.budget.buddy.transaction.domain.service.AccountData;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.domain.vo.MoneyVO;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.AccountTypeGroupRepository;
import com.budget.buddy.transaction.infrastructure.view.AccountFlatView;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountDataImpl implements AccountData {
    private final AccountRepository accountRepository;
    private final AccountTypeGroupRepository accountTypeGroupRepository;
    private final TransactionUtils transactionUtils;

    private static final Logger logger = LogManager.getLogger(AccountDataImpl.class);

    @Transactional
    @Override
    public void createAccount(AccountDTO accountDTO) {
        logger.info("Creating account: name='{}', type='{}'", accountDTO.name(), accountDTO.type());

        MoneyVO moneyVO = new MoneyVO(accountDTO.balance(), accountDTO.currency());
        AccountTypeGroup accountTypeGroup = createOrGetAccountTypeGroup(accountDTO.type());
        List<Account> accounts = accountTypeGroup.getAccounts();
        logger.info("Found {} existing accounts in group '{}'", accounts.size(), accountDTO.type());

        boolean accountExists = accounts.stream()
                .anyMatch(a -> a.getName().equals(accountDTO.name()));
        if (accountExists) {
            logger.info("Account '{}' already exists in group '{}'", accountDTO.name(), accountDTO.type());
            return;
        }

        Account newAccount = new Account(
                accountTypeGroup,
                accountDTO.name(),
                moneyVO);
        accountRepository.save(newAccount);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AccountRetrieveResponse> retrieveAccounts() {
        Long userId = transactionUtils.getCurrentUserId();

        logger.info("Retrieving all accounts for current user");
        List<AccountFlatView> flatList = accountRepository.retreiveAllAccounts(userId);

        List<AccountRetrieveResponse> results = flatList.stream()
                .collect(Collectors.groupingBy(AccountFlatView::getGroupName, TreeMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> {
                    String groupName = entry.getKey();
                    List<AccountDTO> accounts = entry.getValue().stream()
                            .map(this::buildAccountDTO)
                            .toList();
                    return new AccountRetrieveResponse(groupName, accounts);
                })
                .toList();

        logger.info("Retrieved {} account type groups", results.size());
        return results;
    }

    @Transactional(readOnly = true)
    @Override
    public AccountRetrieveResponse retrieveAccount(Long accountId) {
        Long userId = transactionUtils.getCurrentUserId();

        logger.info("Retrieving account with id='{}' for user with id='{}'", accountId, userId);
        AccountFlatView accounts = accountRepository.retrieveByAccountId(userId, accountId);
        if (accounts == null) {
            logger.info("Account with id='{}' not found for user with id='{}'", accountId, userId);
            throw new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        AccountDTO accountDTO = buildAccountDTO(accounts);

        AccountRetrieveResponse response = new AccountRetrieveResponse();
        response.setAccountType(accounts.getGroupName());
        response.setAccounts(List.of(accountDTO));

        logger.info("Retrieved account with id='{}'", accountId);
        return response;
    }

    @Transactional
    @Override
    public void deleteAccount(Long accountId) {
        checkAccountExists(accountId);
        logger.info("Deleting account with id='{}' for user", accountId);
        accountRepository.deleteById(accountId);
    }

    @Transactional
    @Override
    public void updateAccount(Long accountId, AccountDTO accountDTO) {
        checkAccountExists(accountId);
        logger.info("Updating account with id='{}' for user", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));

        MoneyVO moneyVO = new MoneyVO(accountDTO.balance(), accountDTO.currency());
        AccountTypeGroup accountTypeGroup = account.getAccountTypeGroup();

        if (!accountTypeGroup.getName().equals(accountDTO.type())) {
            String fromGroup = accountTypeGroup.getName();
            logger.info("Account type group change detected for id='{}': '{}' -> '{}'", accountId, fromGroup, accountDTO.type());
            accountTypeGroup = createOrGetAccountTypeGroup(accountDTO.type());
            account.setAccountTypeGroup(accountTypeGroup);
        }

        account.setMoney(moneyVO);
        account.setName(accountDTO.name());
        accountRepository.save(account);
        logger.info("Account updated successfully: id='{}'", accountId);
    }

    private void checkAccountExists(Long accountId) {
        Long userId = transactionUtils.getCurrentUserId();

        boolean accountExisted = accountRepository.existsAccountBy(userId, accountId);
        if (!accountExisted) {
            logger.info("Account with id='{}' not existed for user with id='{}'", accountId, userId);
            throw new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
    }

    private AccountDTO buildAccountDTO(AccountFlatView view) {
        return new AccountDTO(
                view.getId(),
                view.getName(),
                view.getAmount(),
                Currency.valueOf(view.getCurrency()),
                null,
                null
        );
    }

    private AccountTypeGroup createOrGetAccountTypeGroup(String name) {
        Long userId = transactionUtils.getCurrentUserId();

        return accountTypeGroupRepository.findByName(name)
                .orElseGet(() -> {
                    AccountTypeGroup newGroup = new AccountTypeGroup(userId, name, new ArrayList<>());
                    logger.info("Creating new account type group: name='{}' for userId='{}'", name, userId);
                    return accountTypeGroupRepository.save(newGroup);
                });
    }
}
