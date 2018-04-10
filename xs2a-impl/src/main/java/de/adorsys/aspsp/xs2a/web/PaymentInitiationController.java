package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.ResponseMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/payments/{product-name}")
public class PaymentInitiationController {

    private final ResponseMapper responseMapper;
    private final PaymentService paymentService;

    @ApiOperation(value = "Initialises a new payment ", notes = "debtor account, creditor accout, creditor name, remittance information unstructured")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "transactions_status received, a list of hyperlinks to be recognized by the Tpp."),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(method = RequestMethod.POST)
    public PaymentInitialisationResponse createPaymentInitiation(
    @ApiParam(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @RequestParam(name = "tppRedirectPreferred", required = false) boolean tppRedirectPreferred,
    @RequestBody SinglePayments paymentInitialisationRequest) {
        // TODO according task PIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/9
        return createResponse();
    }

    @ApiOperation(value = "Get information  about the status of a payment initialisation ")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "transactions_status Accepted Customer Profile."),
    @ApiResponse(code = 404, message = "Not found")})
    @RequestMapping(value = "/{paymentId}/status", method = RequestMethod.GET)
    public ResponseEntity<Map<String, TransactionStatus>> getPaymentInitiationStatusById(
    @ApiParam(name = "paymentId", value = "Resource Identification of the related payment")
    @PathVariable("paymentId") String paymentId) {
        return responseMapper.okOrNotFound(paymentService.getPaymentStatusById(paymentId));
    }

    private PaymentInitialisationResponse createResponse() {
        // TODO according task PIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/9
        return null;
    }

}


