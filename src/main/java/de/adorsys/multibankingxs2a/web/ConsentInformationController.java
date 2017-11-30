package de.adorsys.multibankingxs2a.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibankingxs2a.domain.Account;
import de.adorsys.multibankingxs2a.domain.ConsentResponse;
import de.adorsys.multibankingxs2a.domain.ResponseGeneral;
import de.adorsys.multibankingxs2a.domain.SingleAccountAccess;
import de.adorsys.multibankingxs2a.domain.Transactions;
import de.adorsys.multibankingxs2a.domain.TransactionsResponse;
import de.adorsys.multibankingxs2a.service.ConsentService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
* Created by aro on 27.11.17
*/

@RestController
@SuppressWarnings("unused")
@RequestMapping(path = "api/v1/consents")
public class ConsentInformationController {
	
	private static final Logger log = LoggerFactory.getLogger(PaymentInitiationController.class);
	
	@Autowired
    private ConsentService paymentService;
	
	
	@ApiOperation(value = "Creats an account information consent resource at the ASPSP regarding access to accounts specified in this request",
			notes = "array of single account access, recurring indicator, validation date, frequency per day")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "transactions_status received, a list of hyperlinks to be recognized by the TPP."),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping( method = RequestMethod.POST)
	public  ResponseGeneral getConsetForAccounts(@RequestBody ConsentResponse consentResponse) {
		
		return createResponse();
	
	}
	
	@ApiOperation(value = "Creats an account information consent resource at the ASPSP to return a list of all accessible accounts",
			notes = "if withBalance is true then the balance is on the list off all payments accounts ")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "a list of hyperlinks to be recognized by the TPP."),
    @ApiResponse(code = 400, message = "Bad request") })
	@RequestMapping(value = "/account-list", method = RequestMethod.POST)
	public  ResponseGeneral getAccountWithConsent( @RequestParam(required = true) Boolean withBalance) {
	
		return createResponse();
	
	}
	
	
	@ApiOperation(value = "Check the status of an account information consent resource",
			notes = "consentID")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "transaction_status: AcceptedTechnicalValidation "),
    @ApiResponse(code = 400, message = "Bad request") })
	@RequestMapping(value = "/{consentID}/status", method = RequestMethod.GET)
	public  ResponseGeneral getStatusForConsent( @PathVariable("consentID") String consentID) {
		
		return createResponse();
	
	}
	
	@ApiOperation(value = " Returns the content of an account information consent object",
			notes = "consentID")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Get the list of accounts for this consent ojbect"),
    @ApiResponse(code = 400, message = "Bad request") })
	@RequestMapping(value = "/{consentID}", method = RequestMethod.GET)
	public   ConsentResponse getAccountsWithConsent(@PathVariable("consentID") String consentID) {
		
		//TO DO...... muss as der MongoDB gelesen werden....
		ConsentResponse consent = new ConsentResponse();
		return consent;
	}
	
	
	@ApiOperation(value = " Delete information consent object",
			notes = "consentID")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Deleted successfull"),
    @ApiResponse(code = 400, message = "Bad request") })
	@RequestMapping(value = "/{consentID}", method = RequestMethod.DELETE)
	 public HttpEntity<Void> deleteConsent(@PathVariable String consentID) {
	     return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	 }
	
	 private ResponseGeneral createResponse() {
		 
		 ResponseGeneral response = new ResponseGeneral();
	    
		 return response;
	 }
	 
	
	   
	
}
