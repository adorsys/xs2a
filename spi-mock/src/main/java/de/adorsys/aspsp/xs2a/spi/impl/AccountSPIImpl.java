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
import de.adorsys.aspsp.xs2a.spi.domain.aic.AccountResponse;
import de.adorsys.aspsp.xs2a.spi.service.AccountSPI;
import de.adorsys.aspsp.xs2a.spi.test.data.MockData;



@Component
public class AccountSPIImpl implements AccountSPI  {

	public List<Account> readAccounts(Boolean withBalance, Boolean psuInvolved) {
		
		List<Account> accounts =  MockData.getAccounts()	;
		
		if ((withBalance != null) && (!withBalance)) {
			for (Account account : accounts) {
				account.setBalances(null);
			}
		}
		
		return accounts;
	
	}

	public Balances readBalances(String accountId, Boolean psuInvolved) {
		
		HashMap<String, Account> accounts =  MockData.getAccountsHashMap()	;
		return ( (Account)accounts.get(accountId)).getBalances();
		
	}

	public AccountReport readTransactions(String accountId, String dateFROM, String dateTo, Boolean psuInvolved) {
		
		List<Transactions> transactions =  MockData.getTransactions();
		AccountReport report = new AccountReport();
		List<Transactions> transactions_booked = new ArrayList<Transactions>();
		List<Transactions> transactions_pending = new ArrayList<Transactions>();
		
		for (Transactions transaction : transactions) {
			if (transaction.getCreditor_account()!= null)  {
				System.out.println("creditor Account:" + transaction.getCreditor_account().getId());
			}
			if (transaction.getDebtor_account()!= null)  {
				System.out.println("creditor Debitor:" + transaction.getDebtor_account().getId());
			}
			System.out.println("accountID" + accountId);
			
			if ( (transaction.getCreditor_account()!= null && transaction.getCreditor_account().getId().trim().equals(accountId.trim())) ||
					(transaction.getDebtor_account() != null && transaction.getDebtor_account().getId().trim().equals(accountId.trim()) )) {
				   
				 if (transaction.getBooking_date() != null) {
					 transactions_booked.add(transaction);
				 } else  {
					transactions_pending.add(transaction);
				 }
			 }		   
		}	
		if (transactions_booked.size()>0) {
			Transactions booked[] = transactions_booked.toArray(new Transactions[transactions_booked.size()]);
			report.setBooked(booked);
		}	
		
		if (transactions_pending.size()>0) {
		
			Transactions pending[] = transactions_pending.toArray(new Transactions[transactions_pending.size()]);
			report.setPending(pending);
		}
		report.set_links(MockData.createEmptyLinks());
		return report;
	}
	
	
}
