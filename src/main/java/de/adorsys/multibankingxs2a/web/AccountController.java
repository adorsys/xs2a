package de.adorsys.multibankingxs2a.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibankingxs2a.domain.Account;
import de.adorsys.multibankingxs2a.domain.Balances;
import de.adorsys.multibankingxs2a.domain.PaymentInitialisationResponse;
import de.adorsys.multibankingxs2a.domain.AccountDataResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
* Created by aro on 28.11.17
*/

@RestController
@SuppressWarnings("unused")
@RequestMapping(path = "api/v1/accounts")
public class AccountController {
	
	private static final Logger log = LoggerFactory.getLogger(AccountController.class);
	
	@ApiOperation(value = "Read a list of all accounts ")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(method = RequestMethod.GET)
	public  Account[]  getAccounts() {
		 return readAccounts();
	 }
	
	@ApiOperation(value = "Read a list of all accounts and balances for a PSU ")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(value = "/with_balances/psu_involved",method = RequestMethod.GET)
	public Account[] getAccountsAndBalancesForPSU() {
		 return readAccounts();
	 }
	
	@ApiOperation(value = "Read a list of all accounts and balances ")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(value = "/with_balances", method = RequestMethod.GET)
	public Account[]  getAccountAndBalances() {
		 return readAccounts();
	 }
	
	@ApiOperation(value = "Read a list of all accounts for a PSU")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(value = "/psu_involved", method = RequestMethod.GET)
	public  Account[]  getAccountForPSU() {
		 return readAccounts();
	 }
	
	@ApiOperation(value = "Read a list of the balances for the given account")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(value = "/{accountId}/balances", method = RequestMethod.GET)
	public  Balances  getBalances(@PathVariable String accountId, @RequestParam(value = "psu_involved", required = false) String psu_involved ){
		 return readBalances();
	}
	
	
	@ApiOperation(value = "Read account data from the given account")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(value = "/{accountId}/transactions", method = RequestMethod.GET)
	public  AccountDataResponse  getTransactionsForAccount(@PathVariable String accountId,
			@RequestParam(value = "date_from", required = true) Date dateFROM,
			@RequestParam(value = "date_to", required = true) Date dateTo,
			@RequestParam(value = "transaction_id", required = false) String transactionID,
			@RequestParam(value = "psu_involved", required = false) Boolean psu_involved ){
		 return readAccountData
				 
				 
				 
				 
				 ();
	}
	
	
	 private Account[] readAccounts() {
		
		 //TO DO... full the list of the accounts objects
		 List<Account> accounts = new ArrayList<Account>();
	
		 return (Account[]) accounts.toArray();
	 }
	 
	 
	 private Balances readBalances() {
			
		 //TO DO... full the list of the balances for the accountID
		Balances balances = new Balances();
	
		return balances;
	 }
	 
	 
	 private AccountDataResponse readAccountData() {
			
		 //TO DO... full the list of the balances for the accountID
		 AccountDataResponse accountData = new AccountDataResponse();
	
		 return accountData;
	 }
}
