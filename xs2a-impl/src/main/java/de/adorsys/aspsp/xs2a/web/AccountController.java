package de.adorsys.aspsp.xs2a.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.aspsp.xs2a.service.AccountService;
import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.aic.AccountResponse;
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
	private AccountService accountService ;
	
	private static final Logger log = LoggerFactory.getLogger(AccountController.class);
	
	@ApiOperation(value = "Reads a list of accounts, with balances where required . It is assumed that a consent of the PSU to this access is already given and stored on the ASPSP system. The addressed list of accounts depends then on the PSU ID and the stored consent addressed by consent-id, respectively the OAuth2 token")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response= AccountResponse[].class),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<AccountResponse[]> getAccounts(
		@ApiParam (name="with-balance",value="If contained, this function reads the list of accessible payment accounts including the balance.")
	    @RequestParam(name="with-balance", required=false) Boolean withBalance,
	    @ApiParam(name="psu-involved",value= "If contained, it is indicated that a PSU has directly asked this account access in real-time. The PSU then might be involved in an additional consent process, if the given consent is not any more sufficient.")
		@RequestParam(name="psu-involved",required=false) Boolean psuInvolved ) {
		
		List<AccountResponse> accounts = accountService.getAccounts(withBalance, psuInvolved);
		
		for (AccountResponse account : accounts) {
			
			 //String balancesLink= 
					// linkTo(methodOn(AccountController.class).getBalances(account.getId(), psuInvolved)).toString();
			String balancesLink = linkTo(AccountController.class).slash(account.getId()).toString() + "/balances";
			
			String transactionsLink = linkTo(AccountController.class).slash(account.getId()).toString() + "/transactions";
			
			System.out.println("balancesLink" + balancesLink);
			 // String transactionsLink= 
				//	 linkTo(methodOn(AccountController.class).getTransactionsForAccount(account.getId(), null, null, psuInvolved)).toString();
			 account.get_links().setBalances(balancesLink);
			 account.get_links().setTransactions(transactionsLink);
		 }
		
		AccountResponse response[] = accounts.toArray(new AccountResponse[accounts.size()]);
		return new ResponseEntity<AccountResponse[]>(response,HttpStatus.OK) ;
		
	}
	
	@ApiOperation(value = "Read a list of the balances for the given account")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response=Balances.class),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(value = "/{account-id}/balances", method = RequestMethod.GET)
	public  ResponseEntity<Balances>  getBalances(
			@PathVariable(name="account-id", required=true) String accountId, 
			@ApiParam(name="psu-involved", value="If contained, it is indicated that a PSU has directly asked this account access in realtime. The PSU then might be involved in an additional consent process, if the given consent is not any more sufficient.")
			@RequestParam(name = "psu-involved", required = false) Boolean psu_involved ){
		 Balances balances =accountService.getBalances( accountId,psu_involved );
		
		return new ResponseEntity<Balances>(balances, HttpStatus.OK);
	
	
	}
		
	@ApiOperation(value = "Reads account data from a given account addressed by \"account-id\".")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response=AccountReport.class),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping(value = "/{account-id}/transactions", method = RequestMethod.GET)
	public  ResponseEntity<AccountReport>  getTransactionsForAccount(@PathVariable(name="account-id") String accountId,
			@ApiParam(name="date_from",value="Starting date of the account statement", example="2017-10-30")
			@RequestParam(name = "date_from", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String dateFrom,
			@ApiParam(name="date_to",value="End date of the account statement", example="2017-11-30")
			@RequestParam(name = "date_to", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String dateTo,
			@ApiParam(name="psu-involved", value="If contained, it is indicating that a PSU has directly asked this account access in real-time. The PSU then might be involved in an additional consent process, if the given consent is not any more sufficient.")
			@RequestParam(name = "psu_involved", required = false) Boolean psu_involved ){
	
		AccountReport accountReport= accountService.getTransactionsForAccount(accountId, dateFrom, dateTo,psu_involved);
		
		String link = linkTo(AccountController.class).slash(accountId).toString();
		accountReport.get_links().setAccount_link(link);
		return new ResponseEntity<AccountReport>(accountReport, HttpStatus.OK);
	}
}
