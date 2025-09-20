package com.budget.buddy.transaction.domain.service.impl;

import com.budget.buddy.core.config.exception.ErrorCode;
import com.budget.buddy.core.config.exception.NotFoundException;
import com.budget.buddy.transaction.application.dto.account.AccountDTO;
import com.budget.buddy.transaction.application.dto.account.AccountRetrieveResponse;
import com.budget.buddy.transaction.application.dto.account.AccountTypeRetrieveResponse;
import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.account.AccountTypeGroup;
import com.budget.buddy.transaction.domain.service.AccountData;
import com.budget.buddy.transaction.domain.utils.TransactionUtils;
import com.budget.buddy.transaction.domain.vo.MoneyVO;
import com.budget.buddy.transaction.infrastructure.repository.AccountRepository;
import com.budget.buddy.transaction.infrastructure.repository.AccountTypeGroupRepository;
import com.budget.buddy.transaction.infrastructure.repository.TransactionRepository;
import com.budget.buddy.transaction.application.dto.account.AccountFlatView;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountDataImpl implements AccountData {
    private final AccountRepository accountRepository;
    private final AccountTypeGroupRepository accountTypeGroupRepository;
    private final TransactionUtils transactionUtils;
    private final TransactionRepository transactionRepository;
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
    public boolean isAccountCurrencyInvalid(AccountDTO accountDTO, Long accountId) {
        logger.info("Checking currency for account: type='{}'", accountDTO.type());

        AccountTypeGroup accountTypeGroup = accountTypeGroupRepository.findByName(accountDTO.type())
                .orElse(null);
        if (accountTypeGroup == null) {
            logger.info("Account type group not found: '{}'", accountDTO.type());
            return false;
        }

        // Only 1 account in a group can have a different currency
        if (accountId != null && accountTypeGroup.getAccounts().size() == 1) {
            return false;
        }

        String currency = accountTypeGroup.getAccounts()
                .stream()
                .findFirst()
                .map(Account::getMoney)
                .map(MoneyVO::getCurrency)
                .orElse(null);
        logger.info("Found currency for account: type='{}': '{}'", accountDTO.type(), currency);
        return currency != null && !currency.equals(accountDTO.currency().name());
    }

    @Transactional(readOnly = true)
    @Override
    public List<AccountRetrieveResponse> retrieveAccounts() {
        Long userId = transactionUtils.getCurrentUserId();

        logger.info("Retrieving all accounts for current user");
        List<AccountFlatView> flatList = accountRepository.retrieveAllAccounts(userId);

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

        logger.info("Retrieved {} account groups", results.size());
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

    @Transactional(readOnly = true)
    @Override
    public List<AccountFlatView> retrieveAccountByIdList(List<Long> accountIds) {
        Long userId = transactionUtils.getCurrentUserId();

        logger.info("Retrieving accounts with id='{}' for user with id='{}'", accountIds, userId);
        return accountRepository.retrieveByAccountIdIn(userId, accountIds);
    }

    @Transactional
    @Override
    public void deleteAccount(Long accountId) {
        logger.info("Deleting account with id='{}' for user", accountId);
        accountRepository.deleteById(accountId);
    }

    @Transactional
    @Override
    public void updateAccount(Long accountId, AccountDTO accountDTO) {
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

    @Override
    public AccountTypeRetrieveResponse getAccountTypeGroups() {
        List<String> accountTypeGroupNames = accountTypeGroupRepository.findAll()
                .stream().map(AccountTypeGroup::getName).toList();
        logger.info("Retrieved {} account type groups", accountTypeGroupNames.size());
        return new AccountTypeRetrieveResponse(accountTypeGroupNames);
    }

    @Transactional
    @Override
    public void deleteAccountTypeGroups(Long groupId) {
        logger.info("Deleting all account type groups");

        AccountTypeGroup groups = accountTypeGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_TYPE_GROUP_NOT_FOUND));

        List<Account> accounts = groups.getAccounts();

        accountRepository.deleteAll(accounts);
        accountTypeGroupRepository.deleteById(groupId);
        logger.info("Deleted all account type groups");
    }

    @Transactional(readOnly = true)
    @Override
    public void checkAccountExists(Long accountId) {
        Long userId = transactionUtils.getCurrentUserId();

        boolean accountExisted = accountRepository.existsAccountBy(userId, accountId);
        if (!accountExisted) {
            logger.info("Account with id='{}' not existed for user with id='{}'", accountId, userId);
            throw new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
    }

    @Transactional
    @Override
    public void updateAvailableBalance(Long accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_NOT_FOUND));
        MoneyVO moneyVO = account.getMoney();
        BigDecimal oldBalance = moneyVO.getAmount();
        newBalance = oldBalance.add(newBalance);
        logger.info("Updating available balance for account with id='{}': {} -> {}", accountId, oldBalance, newBalance);

        MoneyVO newMoney = new MoneyVO(newBalance, Currency.valueOf(moneyVO.getCurrency()));
        account.setMoney(newMoney);
        accountRepository.save(account);
        logger.info("Updated available balance for account with id='{}'", accountId);
    }

    @Transactional
    @Override
    public void transferMoney(Long sourceAccountId, Long targetAccountId, BigDecimal newBalance) {
        List<Long> accountIds = List.of(sourceAccountId, targetAccountId);
        List<Account> accounts = accountRepository.findByIdIn(accountIds);
        Map<Long, Account> accountMap = accounts.stream().collect(Collectors.toMap(Account::getId, a -> a));

        logger.info("Transfer money from account with id='{}' to account with id='{}': {}", sourceAccountId, targetAccountId, newBalance);

        // Source account
        Account sourceAccount = accountMap.get(sourceAccountId);

        MoneyVO sourceMoney = sourceAccount.getMoney();
        BigDecimal sourceBalance = sourceMoney.getAmount();

        String currency = sourceMoney.getCurrency();

        BigDecimal newSourceBalance = sourceBalance.subtract(newBalance);
        MoneyVO newSourceMoney = new MoneyVO(newSourceBalance, Currency.valueOf(currency));

        sourceAccount.setMoney(newSourceMoney);
        accountRepository.save(sourceAccount);

        // Target account
        Account targetAccount = accountMap.get(targetAccountId);

        MoneyVO currentTargetMoney = targetAccount.getMoney();
        BigDecimal currentTargetBalance = currentTargetMoney.getAmount();

        BigDecimal newTargetAccountBalance = currentTargetBalance.add(newBalance);
        MoneyVO newTargetMoney = new MoneyVO(newTargetAccountBalance, Currency.valueOf(currency));

        targetAccount.setMoney(newTargetMoney);
        accountRepository.save(targetAccount);

        logger.info("New balances for accounts: from account ='{}', to account ='{}'", newSourceBalance, newTargetAccountBalance);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isTransactionExistedByAccountId(Long accountId) {
        return transactionRepository.existsBySourceAccountId(accountId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isTransactionExistedByGroupAccountId(Long groupId) {
        AccountTypeGroup groups = accountTypeGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_TYPE_GROUP_NOT_FOUND));
        List<Account> accounts = groups.getAccounts();
        List<Long> accountIds = accounts.stream().map(Account::getId).toList();
        return transactionRepository.existsBySourceAccountIdIn(accountIds);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> getAccountIdsByGroupId(Long groupId) {
        AccountTypeGroup groups = accountTypeGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ACCOUNT_TYPE_GROUP_NOT_FOUND));
        List<Account> accounts = groups.getAccounts();
        return accounts.stream().map(Account::getId).toList();
    }

    private AccountDTO buildAccountDTO(AccountFlatView view) {
        return new AccountDTO(
                view.getId(),
                view.getName(),
                view.getAmount(),
                Currency.valueOf(view.getCurrency()),
                null,
                view.getGroupId()
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
