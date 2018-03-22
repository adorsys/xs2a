package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.Transaction;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountSpiImpl implements AccountSpi {

    @Override
    public List<SpiAccountDetails> readAccounts(boolean withBalance, boolean psuInvolved) {

        if (!withBalance) {
            return getNoBalanceAccountList(AccountMockData.getAccountDetails());
        }

        return AccountMockData.getAccountDetails();
    }

    private List<SpiAccountDetails> getNoBalanceAccountList(List<SpiAccountDetails> accountDetails) {
        return accountDetails.stream()
               .map(account -> AccountMockData.createAccount(
               account.getId(),
               account.getCurrency(),
               null,
               account.getIban(),
               account.getBic(),
               account.getName(),
               account.getAccountType())).collect(Collectors.toList());
    }

    @Override
    public SpiBalances readBalances(String accountId, boolean psuInvolved) {
        SpiBalances balances = null;
        SpiAccountDetails accountDetails = AccountMockData.getAccountsHashMap().get(accountId);

        if (accountDetails != null) {
            balances = accountDetails.getBalances();
        }

        return balances;
    }

    @Override
    public List<Transaction> readTransactionsByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved) {
        List<Transaction> transactions = AccountMockData.getTransactions();

        List<Transaction> validTransactions = filterValidTransactionsByAccountId(transactions, accountId);
        List<Transaction> transactionsFilteredByPeriod = filterTransactionsByPeriod(validTransactions, dateFrom, dateTo);

        return Collections.unmodifiableList(transactionsFilteredByPeriod);
    }

    @Override
    public List<Transaction> readTransactionsById(String accountId, String transactionId, boolean psuInvolved) {
        List<Transaction> transactions = AccountMockData.getTransactions();

        List<Transaction> validTransactions = filterValidTransactionsByAccountId(transactions, accountId);
        List<Transaction> filteredTransactions = filterValidTransactionsByTransactionId(validTransactions, transactionId);

        return Collections.unmodifiableList(filteredTransactions);
    }

    private Transaction[] getFilteredPendingTransactions(List<Transaction> transactions) {
        return transactions.parallelStream()
               .filter(this::isPendingTransaction)
               .toArray(Transaction[]::new);
    }

    private Transaction[] getFilteredBookedTransactions(List<Transaction> transactions) {
        return transactions.parallelStream()
               .filter(transaction -> !isPendingTransaction(transaction))
               .toArray(Transaction[]::new);
    }

    private boolean isPendingTransaction(Transaction transaction) {
        return transaction.getBookingDate() == null;
    }

    private List<Transaction> filterTransactionsByPeriod(List<Transaction> transactions, Date dateFrom, Date dateTo) {
        return transactions.parallelStream()
               .filter(transaction -> isDateInTimeFrame(transaction.getBookingDate(), dateFrom, dateTo))
               .collect(Collectors.toList());
    }

    private static boolean isDateInTimeFrame(Date currentDate, Date dateFrom, Date dateTo) {
        return currentDate != null && currentDate.after(dateFrom) && currentDate.before(dateTo);
    }

    private List<Transaction> filterValidTransactionsByAccountId(List<Transaction> transactions, String accountId) {
        return transactions.parallelStream()
               .filter(transaction -> transactionIsValid(transaction, accountId))
               .collect(Collectors.toList());
    }

    private List<Transaction> filterValidTransactionsByTransactionId(List<Transaction> transactions, String transactionId) {
        return transactions.parallelStream()
               .filter(transaction -> transactionId.equals(transaction.getTransactionId()))
               .collect(Collectors.toList());
    }

    private boolean transactionIsValid(Transaction transaction, String accountId) {

        boolean isCreditorAccountValid = Optional.ofNullable(transaction.getCreditorAccount())
                                         .map(creditorAccount -> creditorAccount.getAccountId().trim().equals(accountId))
                                         .orElse(false);

        boolean isDebtorAccountValid = Optional.ofNullable(transaction.getDebtorAccount())
                                       .map(debtorAccount -> debtorAccount.getAccountId().trim().equals(accountId))
                                       .orElse(false);

        return isCreditorAccountValid || isDebtorAccountValid;
    }
}
