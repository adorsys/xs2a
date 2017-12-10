package de.adorsys.aspsp.xs2a.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.service.AccountSPI;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
* Created by aro on 28.11.17
*/

@RestController
@SuppressWarnings("unused")
@RequestMapping(path = "api/v1/accounts")
@Api(value="api/v1/accounts", tags="AISP, Accounts", description="Provides access to the PSU account")
public class AccountController {
	
	@Autowired
	private AccountSPI accountSPI;
	
	private static final Logger log = LoggerFactory.getLogger(AccountController.class);
	
	@ApiOperation(value = "Reads a list of accounts, with balances where required . It is assumed that a consent of the PSU to this access is already given and stored on the ASPSP system. The addressed list of accounts depends then on the PSU ID and the stored consent addressed by consent-id, respectively the OAuth2 token")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response= Account[].class),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(method = RequestMethod.GET)
	public  ResponseEntity<Account[]>  getAccounts(
					 @ApiParam (name="with-balance",value="If contained, this function reads the list of accessible payment accounts including the balance.")
			         @RequestParam(name="with-balance", required=false) Boolean withBalance,
			         @ApiParam(name="psu-involved",value= "If contained, it is indicated that a PSU has directly asked this account access in real-time. The PSU then might be involved in an additional consent process, if the given consent is not any more sufficient.")
					 @RequestParam(name="psu-involved",required=false) Boolean psuInvolved ) {
		 return new ResponseEntity<Account[]>(readAccounts(withBalance, psuInvolved),HttpStatus.OK);
	 }
	
	@ApiOperation(value = "Read a list of the balances for the given account")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response=Balances.class),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(value = "/{account-id}/balances", method = RequestMethod.GET)
	public  ResponseEntity<Balances>  getBalances(
			@PathVariable(name="account-id", required=true) String accountId, 
			@ApiParam(name="psu-involved", value="If contained, it is indicated that a PSU has directly asked this account access in realtime. The PSU then might be involved in an additional consent process, if the given consent is not any more sufficient.")
			@RequestParam(name = "psu-involved", required = false) Boolean psu_involved ){
		 return new ResponseEntity<Balances>(readBalances(psu_involved), HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "Reads account data from a given account addressed by \"account-id\".")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response=AccountReport.class),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(value = "/{account-id}/transactions", method = RequestMethod.GET)
	public  ResponseEntity<AccountReport>  getTransactionsForAccount(@PathVariable(name="account-id") String accountId,
			@ApiParam(name="date_from",value="Starting date of the account statement", example="2017-10-30")
			@RequestParam(name = "date_from", required = true) Date dateFROM,
			@ApiParam(name="date_to",value="End date of the account statement", example="2017-11-30")
			@RequestParam(name = "date_to", required = true) Date dateTo,
			@ApiParam(name="transaction_id",value="This data attribute is indicating that the AISP is in favour to get all transactions after the transaction with identification transaction_id alternatively to the above defined period. (Implementation of a delta-report). If this data element is contained, the entries \"date_from\" and \"date_to\" might be ignored by the ASPSP if a delta report is supported.")
			@RequestParam(name = "transaction_id", required = false) String transactionID,
			@ApiParam(name="psu-involved", value="If contained, it is indicating that a PSU has directly asked this account access in real-time. The PSU then might be involved in an additional consent process, if the given consent is not any more sufficient.")
			@RequestParam(name = "psu_involved", required = false) Boolean psu_involved ){
		 return new ResponseEntity<AccountReport>(readTransactions(accountId,dateFROM,dateTo,transactionID,psu_involved), HttpStatus.OK);
 
	}
	
	private AccountReport readTransactions(String accountId, Date dateFROM, Date dateTo, String transactionID,
			Boolean psuInvolved) {
		
		AccountReport accountReport = accountSPI.readTransactions(accountId, dateFROM, dateTo, transactionID, psuInvolved);
		String link = linkTo(AccountController.class).slash(accountId).toString();
		accountReport.get_links().setAccount_link(link);
		return null;
	}



	private Account[] readAccounts(Boolean withBalance, Boolean psuInvolved) {
		
		// Read from SPI
		List<Account> accounts = accountSPI.readAccounts(withBalance, psuInvolved);
		 for (Account account : accounts) {
			 String balances= 
					 linkTo(methodOn(AccountController.class).getBalances(account.getId(), psuInvolved)).toString();
			 String transactions= 
					 linkTo(methodOn(AccountController.class).getTransactionsForAccount(account.getId(), null, null, null, psuInvolved)).toString();
			 //methodOn(PaymentController.class).getPyament(paymentEntity.getId()))
			account.get_links().setBalances(balances);
			account.get_links().setTransactions(transactions);
		 }
		 return (Account[]) accounts.toArray();
	 }
	 
	 private Balances readBalances(Boolean psuInvolved) {
			
		 // Sample comment
		Balances balances = accountSPI.readBalances(psuInvolved);
	
		return balances;
	 }
	 
	 
	
}
