package de.adorsys.multibankingxs2a.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.multibankingxs2a.domain.RequestFunds;
import de.adorsys.multibankingxs2a.domain.ResponseGeneral;
import de.adorsys.multibankingxs2a.domain.Transactions;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
* Created by aro on 28.11.17
*/

@RestController
@SuppressWarnings("unused")
@RequestMapping(path = "api/v1/confirmation-of-funds")
public class ConfirmationFundsController {
	
	private static final Logger log = LoggerFactory.getLogger(ConfirmationFundsController.class);
	
	@ApiOperation(value = "Create a confirmation of funds request ", notes = "debtor account, creditor accout, creditor name, remittance information unstructured")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "return true or false"),
    @ApiResponse(code = 400, message = "Bad request") })
    @RequestMapping( method = RequestMethod.POST)
	public  Boolean getConfirmationOFunds(@RequestBody RequestFunds request) {
		return false;
	}
	
	
}
