package de.adorsys.aspsp.xs2a.service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.aic.AccountResponse;
import de.adorsys.aspsp.xs2a.spi.impl.AccountSPIImpl;
import de.adorsys.aspsp.xs2a.spi.service.AccountSPI;
import de.adorsys.aspsp.xs2a.web.AccountController;
import io.swagger.annotations.ApiParam;


/**
 * Created by aro on 27.11.17.
 */
@Service
public class AccountService {
	
	@Autowired
	private AccountSPIImpl bankServiceProvider = new AccountSPIImpl();
	private AccountSPI accountSPI = (AccountSPI) bankServiceProvider;
	
	public  List<AccountResponse> getAccounts(Boolean withBalance, Boolean psuInvolved) {
		
		List<Account> accounts = readAccounts(withBalance, psuInvolved);
		List <AccountResponse> liste = new ArrayList<AccountResponse>();
		for (Account account : accounts) {
			AccountResponse accountResponse = new AccountResponse();
			accountResponse.setId(account.getId());
			accountResponse.setIban(account.getIban());
			accountResponse.setCurrency(account.getCurrency());
			accountResponse.setAccount_type(account.getAccount_type());
			accountResponse.set_links(account.get_links());
			liste.add(accountResponse);
		}
		return liste;
	}
	
	public Balances getBalances( String accountId, Boolean psu_involved ) {
		return readBalances(accountId,psu_involved );
	}
	
	public  AccountReport getTransactionsForAccount( String accountId, String dateFROM,
													String dateTo, 
													Boolean psu_involved ){
		 return readTransactions( accountId, dateFROM, dateTo,psu_involved);
	
	}
	
	
	
	private Balances readBalances(String accountId, Boolean psuInvolved ) {

		return accountSPI.readBalances(accountId, psuInvolved);
		
	}
	
	private List<Account> readAccounts(Boolean withBalance, Boolean psuInvolved) {
		
		return accountSPI.readAccounts(withBalance, psuInvolved);
		
	 }
	
	private AccountReport readTransactions(String accountId, String dateFROM, String dateTo, Boolean psuInvolved) {
		
		return accountSPI.readTransactions(accountId, dateFROM, dateTo, psuInvolved);
		
	}

	

	
}
