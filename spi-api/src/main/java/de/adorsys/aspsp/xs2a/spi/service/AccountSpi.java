package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.Transaction;

import java.util.Date;
import java.util.List;

public interface AccountSpi {
    List<SpiAccountDetails> readAccounts(boolean withBalance, boolean psuInvolved);
    SpiBalances readBalances(String accountId, boolean psuInvolved);
    List<Transaction> readTransactionsByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved);
    List<Transaction> readTransactionsById(String accountId, String transactionId, boolean psuInvolved);
}
