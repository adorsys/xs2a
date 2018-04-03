package de.adorsys.aspsp.xs2a.web.fund;

import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import static de.adorsys.aspsp.xs2a.util.Routing.FUNDS_CONFIRMATION_BASE_URL;

@Api(value = FUNDS_CONFIRMATION_BASE_URL, tags = "AISP, Funds confirmation", description = "Provides access to the funds confirmation")
public interface IFundsConfirmationController {

    @ApiOperation(value = "Create a confirmation of funds request ", notes = "debtor account, creditor account, creditor name, remittance information unstructured")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "return true or false"), @ApiResponse(code = 400, message = "Bad request")})
    FundsConfirmationResponse fundConfirmation(FundsConfirmationRequest request);
}
