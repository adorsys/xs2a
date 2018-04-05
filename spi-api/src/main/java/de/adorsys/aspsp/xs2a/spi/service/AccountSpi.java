package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;

import java.util.Date;
import java.util.List;

public interface AccountSpi {
    List<SpiAccountDetails> readAccounts(boolean withBalance, boolean psuInvolved);

    List<SpiBalances> readBalances(String accountId, boolean psuInvolved);

    List<SpiTransaction> readTransactionsByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved);

    List<SpiTransaction> readTransactionsById(String accountId, String transactionId, boolean psuInvolved);
}
