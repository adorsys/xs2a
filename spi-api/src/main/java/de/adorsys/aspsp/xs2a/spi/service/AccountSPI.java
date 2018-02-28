package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;

import java.util.Date;
import java.util.List;

public interface AccountSPI {
    List<Account> readAccounts(boolean withBalance, boolean psuInvolved);
    Balances readBalances(String accountId, boolean psuInvolved);
    AccountReport readTransactionsByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved);
    AccountReport readTransactionsById(String accountId, String transactionId, boolean psuInvolved);
}
