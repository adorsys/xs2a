package de.adorsys.aspsp.xs2a.spi.impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.service.AccountSPI;

@Component
public class AccountSPIImpl implements AccountSPI {

	public List<Account> readAccounts(Boolean withBalance, Boolean psuInvolved) {
		// TODO Auto-generated method stub
		return null;
	}

	public Balances readBalances(Boolean psuInvolved) {
		// TODO Auto-generated method stub
		return null;
	}

	public AccountReport readTransactions(String accountId, Date dateFROM, Date dateTo, String transactionID,
			Boolean psuInvolved) {
		// TODO Auto-generated method stub
		return null;
	}

}
