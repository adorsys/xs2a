package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.service.FundsConfirmationService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/funds-confirmations")
@Api(value = "api/v1/funds-confirmations", tags = "AISP Funds confirmation", description = "Provides access to the funds confirmation")
public class FundsConfirmationController {
    private final FundsConfirmationService fundsConfirmationService;
    private final ResponseMapper responseMapper;

    @PostMapping
    @ApiOperation(value = "Create a confirmation of funds request ", notes = "debtor account, creditor account, creditor name, remittance information unstructured")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "return true or false"), @ApiResponse(code = 400, message = "Bad request")})
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<FundsConfirmationResponse> fundConfirmation(@RequestBody FundsConfirmationRequest request) {

        return responseMapper.okOrNotFound(fundsConfirmationService.fundsConfirmation(request));
    }
}
