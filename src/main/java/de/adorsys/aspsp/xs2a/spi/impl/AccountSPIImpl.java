package de.adorsys.aspsp.xs2a.spi.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;


import org.springframework.stereotype.Component;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Amount;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.SingleBalance;
import de.adorsys.aspsp.xs2a.spi.domain.Transactions;
import de.adorsys.aspsp.xs2a.spi.service.AccountSPI;
import de.adorsys.aspsp.xs2a.spi.test.data.MockData;



@Component
public class AccountSPIImpl implements AccountSPI  {

	public List<Account> readAccounts(Boolean withBalance, Boolean psuInvolved) {
		
		List<Account> accounts =  MockData.getAccounts()	;
		
		if (!withBalance) {
			for (Account account : accounts) {
				account.setBalances(null);
			}
		}
		
		return accounts;
	
	}

	public Balances readBalances(String accountId, Boolean psuInvolved) {
		
		HashMap accounts =  MockData.getAccountHashMap()	;
		return ( (Account)accounts.get(accountId)).getBalances();
		
	}

	public AccountReport readTransactions(String accountId, Date dateFROM, Date dateTo, Boolean psuInvolved) {
		
		List<Transactions> transactions =  MockData.getTransactions();
		AccountReport report = new AccountReport();
		List<Transactions> transactions_booked = new ArrayList<Transactions>();
		List<Transactions> transactions_pending = new ArrayList<Transactions>();
		
		for (Transactions transaction : transactions) {
			   if (transaction.getCreditor_account().getId().equals(accountId) || transaction.getDebtor_account().equals(accountId) ) {
				   
				 if (transaction.getBooking_date() != null) {
					 transactions_booked.add(transaction);
				 } else  {
					 transactions_pending.add(transaction);
				 }
				  
				  
			   }		   
			}	
		report.setBooked((Transactions[])transactions_booked.toArray());
		report.setPending((Transactions[])transactions_pending.toArray());
		
		return report;
	}
	
	
}
