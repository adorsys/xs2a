package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;

import java.util.List;

public interface AccountSPI {
    List<Account> readAccounts(Boolean withBalance, Boolean psuInvolved);
    Balances readBalances(String accountId, Boolean psuInvolved);
    AccountReport readTransactions(String accountId, String dateFrom, String dateTo, Boolean psuInvolved);
}
