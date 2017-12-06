
package de.adorsys.multibankingxs2a.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibankingxs2a.service.PaymentService;

import de.adorsys.multibankingxs2a.domain.*;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;;


/**
 * Created by aro on 23.11.17
 */

@RestController
@SuppressWarnings("unused")
@RequestMapping(path = "api/v1/payments/{product -name}")

public class PaymentInitiationController {
	
	private static final Logger log = LoggerFactory.getLogger(PaymentInitiationController.class);
	
	 
	@ApiOperation(value = "Initialises a new payment ", notes = "debtor account, creditor accout, creditor name, remittance information unstructured")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "transactions_status received, a list of hyperlinks to be recognized by the TPP."),
    @ApiResponse(code = 400, message = "Bad request") })
   
	@RequestMapping( method = RequestMethod.POST)
	public  PaymentInitialisationResponse createPaymentInitiation(@RequestBody PaymentInitialisationRequest aymentInitialisationRequest) {
		return createResponse();
	}
	
	@ApiOperation(value = "Get information  about the status of a payment initialisation ", notes = "the payment ID")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "transactions_status Accepted Customer Profil."),
    @ApiResponse(code = 400, message = "Bad request") })
	@RequestMapping(value = "/{paymentId}/status", method = RequestMethod.GET)
	public TransactionStatus getPaymentInitiationStatus(@PathVariable String paymentId) {
		 return TransactionStatus.RCVD;
    }
	 
	@ApiOperation(value = "Get information  about all payments ", notes = "the payment ID")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "transactions_status ?????"),
    @ApiResponse(code = 400, message = "Bad request") })
	@RequestMapping(value = "/{paymentId}", method = RequestMethod.GET)
	public Transactions  getPyamentIniatiation(@PathVariable String paymentId) {
		
		// TO DO... we have get back here  a Resource<PaymentInitialisationEntity>
		return new Transactions();
	}
	
	
	 
	 private PaymentInitialisationResponse createResponse() {
		    
		 PaymentInitialisationResponse response = new PaymentInitialisationResponse();
	    //	return new Resource<>(transactions,
	      //             linkTo(methodOn(PaymentInitiationController.class).paymentInitiation(transactions.getTransaction_id())).withSelfRel());
	        
		 return response;
	 }

}

