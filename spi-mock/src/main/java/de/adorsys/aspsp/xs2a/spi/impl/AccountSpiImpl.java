package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.*;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountSpiImpl implements AccountSpi {
    private final Logger LOGGER = LoggerFactory.getLogger(AccountSpiImpl.class);

    public List<Account> readAccounts(boolean withBalance, boolean psuInvolved) {

        if (!withBalance) {
            return getNoBalanceAccountList(AccountMockData.getAccounts());
        }

        return AccountMockData.getAccounts();
    }

    private List<Account> getNoBalanceAccountList(List<Account> accounts) {
        return accounts.stream().map(account -> AccountMockData.createAccount(
        account.getId(),
        account.getCurrency(),
        null,
        account.getIban(),
        account.getBic(),
        account.getName(),
        account.getAccount_type())).collect(Collectors.toList());
    }

    public Balances readBalances(String accountId, boolean psuInvolved) {
        HashMap<String, Account> accounts = AccountMockData.getAccountsHashMap();
        Account account = Optional.ofNullable(accounts.get(accountId)).orElse(new Account());
        return account.getBalances();
    }

    public AccountReport readTransactionsByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved) {
        List<Transactions> transactions = AccountMockData.getTransactions();
        Links links = AccountMockData.createEmptyLinks();

        List<Transactions> validTransactions = filterValidTransactionsByAccountId(transactions, accountId);
        List<Transactions> transactionsFilteredByPeriod = filterTransactionsByPeriod(validTransactions, dateFrom, dateTo);
        Transactions[] bookedTransactions = getFilteredBookedTransactions(transactionsFilteredByPeriod);
        Transactions[] pendingTransactions = getFilteredPendingTransactions(transactionsFilteredByPeriod);

        return new AccountReport(bookedTransactions, pendingTransactions, links);
    }

    public AccountReport readTransactionsById(String accountId, String transactionId, boolean psuInvolved) {
        List<Transactions> transactions = AccountMockData.getTransactions();
        Links links = AccountMockData.createEmptyLinks();

        List<Transactions> validTransactions = filterValidTransactionsByAccountId(transactions, accountId);
        List<Transactions> filteredTransaction = filterValidTransactionsByTransactionId(validTransactions, transactionId);

        Transactions[] bookedTransactions = getFilteredBookedTransactions(filteredTransaction);
        Transactions[] pendingTransactions = getFilteredPendingTransactions(filteredTransaction);

        return new AccountReport(bookedTransactions, pendingTransactions, links);
    }

    private Transactions[] getFilteredPendingTransactions(List<Transactions> transactions) {
        return transactions.parallelStream()
               .filter(transaction -> transaction.getBooking_date() == null)
               .toArray(Transactions[]::new);
    }

    private Transactions[] getFilteredBookedTransactions(List<Transactions> transactions) {
        return transactions.parallelStream()
               .filter(transaction -> transaction.getBooking_date() != null)
               .toArray(Transactions[]::new);
    }

    private List<Transactions> filterTransactionsByPeriod(List<Transactions> transactions, Date dateFrom, Date dateTo) {
        return transactions.parallelStream()
               .filter(transaction ->
                       transaction.getBooking_date().after(dateFrom)
                       && transaction.getBooking_date().before(dateTo)
               )
               .collect(Collectors.toList());
    }

    private List<Transactions> filterValidTransactionsByAccountId(List<Transactions> transactions, String accountId) {
        return transactions.parallelStream()
               .filter(transaction -> transactionIsValid(transaction, accountId))
               .collect(Collectors.toList());
    }

    private List<Transactions> filterValidTransactionsByTransactionId(List<Transactions> transactions, String transactionId) {
        return transactions.parallelStream()
               .filter(transaction -> transaction.getTransaction_id().equals(transactionId))
               .collect(Collectors.toList());
    }

    private boolean transactionIsValid(Transactions transaction, String accountId) {

        boolean isCreditorAccountValid = Optional.ofNullable(transaction.getCreditor_account())
                                         .map(creditorAccount -> creditorAccount.getId().trim().equals(accountId.trim()))
                                         .orElse(false);

        boolean isDebtorAccountValid = Optional.ofNullable(transaction.getDebtor_account())
                                       .map(debtorAccount -> debtorAccount.getId().trim().equals(accountId.trim()))
                                       .orElse(false);

        return isCreditorAccountValid || isDebtorAccountValid;
    }
}
