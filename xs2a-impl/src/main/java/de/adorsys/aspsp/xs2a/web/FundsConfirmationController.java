package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.service.FundsConfirmationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "api/v1/funds-confirmations")
@Api(value = "api/v1/funds-confirmations", tags = "AISP Funds confirmation", description = "Provides access to the funds confirmation")
public class FundsConfirmationController {
    private FundsConfirmationService fundsConfirmationService;

    @Autowired
    public FundsConfirmationController(FundsConfirmationService fundsConfirmationService) {
        this.fundsConfirmationService = fundsConfirmationService;
    }

    @PostMapping
    @ApiOperation(value = "Create a confirmation of funds request ", notes = "debtor account, creditor account, creditor name, remittance information unstructured")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "return true or false"), @ApiResponse(code = 400, message = "Bad request")})
    public FundsConfirmationResponse fundConfirmation(@RequestBody FundsConfirmationRequest request) {
        return new FundsConfirmationResponse(fundsConfirmationService.fundsConfirmation(request));
    }
}
