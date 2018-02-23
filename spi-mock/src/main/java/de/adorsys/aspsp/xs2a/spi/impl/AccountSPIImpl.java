package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.*;
import de.adorsys.aspsp.xs2a.spi.service.AccountSPI;
import de.adorsys.aspsp.xs2a.spi.test.data.MockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountSPIImpl implements AccountSPI {
    private final Logger LOGGER = LoggerFactory.getLogger(AccountSPIImpl.class);

    public List<Account> readAccounts(Boolean withBalance, Boolean psuInvolved) {

        List<Account> accounts = MockData.getAccounts();

        if ((withBalance != null) && (!withBalance)) {
            for (Account account : accounts) {
                account.setBalances(null);
            }
        }

        return accounts;
    }

    public Balances readBalances(String accountId, Boolean psuInvolved) {
        HashMap<String, Account> accounts = MockData.getAccountsHashMap();
        return (accounts.get(accountId)).getBalances();
    }

    public AccountReport readTransactions(String accountId, String dateFrom, String dateTo, Boolean psuInvolved) {
        // Todo replace mock data with real transactions
        List<Transactions> transactions = MockData.getTransactions();
        Links links = MockData.createEmptyLinks();

        List<Transactions> validTransactions = filterValidTransactions(transactions, accountId);
        Transactions[] bookedTransactions = getFilteredBookedTransactions(validTransactions);
        Transactions[] pendingTransactions = getFilteredPendingTransactions(validTransactions);

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

    private List<Transactions> filterValidTransactions(List<Transactions> transactions, String accountId) {
        return transactions.parallelStream()
                .filter(transaction -> transactionIsValid(transaction, accountId))
                .collect(Collectors.toList());
    }

    private boolean transactionIsValid(Transactions transaction, String accountId) {

        Boolean isCreditorAccountValid = Optional.ofNullable(transaction.getCreditor_account())
                .map(creditorAccount -> creditorAccount.getId().trim().equals(accountId.trim()))
                .orElse(false);

        Boolean isDebtorAccountValid = Optional.ofNullable(transaction.getDebtor_account())
                .map(debtorAccount -> debtorAccount.getId().trim().equals(accountId.trim()))
                .orElse(false);

        return isCreditorAccountValid || isDebtorAccountValid;
    }
}
