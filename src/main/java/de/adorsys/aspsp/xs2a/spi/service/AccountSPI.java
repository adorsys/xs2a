package de.adorsys.aspsp.xs2a.spi.service;

import java.util.Date;
import java.util.List;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;

public interface AccountSPI {
	public List<Account> readAccounts(Boolean withBalance, Boolean psuInvolved);
	public Balances readBalances(String accountId, Boolean psuInvolved);
	public AccountReport readTransactions(String accountId, Date dateFROM, Date dateTo,Boolean psuInvolved);
}
