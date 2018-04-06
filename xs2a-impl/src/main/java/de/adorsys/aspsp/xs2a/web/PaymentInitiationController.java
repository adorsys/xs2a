
package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.aspsp.xs2a.domain.Transactions;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.ResponseMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/payments/{product-name}")
public class PaymentInitiationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentInitiationController.class);
    private ResponseMapper responseMapper;
    private PaymentService paymentService;

    @Autowired
    public PaymentInitiationController(PaymentService paymentService,ResponseMapper responseMapper) {
        this.paymentService = paymentService;
        this.responseMapper = responseMapper;
    }

    @ApiOperation(value = "Initialises a new payment ", notes = "debtor account, creditor accout, creditor name, remittance information unstructured")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "transactions_status received, a list of hyperlinks to be recognized by the Tpp."),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(method = RequestMethod.POST)
    public PaymentInitiationResponse createPaymentInitiation(
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
        ResponseEntity responseEntity = responseMapper.okOrNotFound(paymentService.getPaymentStatusById(paymentId));
        return responseEntity;
    }

    private PaymentInitiationResponse createResponse() {
        // TODO according task PIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/9
        return null;
    }

}


