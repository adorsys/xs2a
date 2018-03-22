
package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.Transaction;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.domain.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/payments/{product-name}")
public class PaymentInitiationController {

    private static final Logger log = LoggerFactory.getLogger(PaymentInitiationController.class);

    @ApiOperation(value = "Initialises a new payment ", notes = "debtor account, creditor accout, creditor name, remittance information unstructured")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "transactions_status received, a list of hyperlinks to be recognized by the Tpp."),
    @ApiResponse(code = 400, message = "Bad request")})

    @RequestMapping(method = RequestMethod.POST)
    public PaymentInitialisationResponse createPaymentInitiation(@RequestBody SinglePayments aymentInitialisationRequest) {
        return createResponse();
    }

    @ApiOperation(value = "Get information  about the status of a payment initialisation ", notes = "the payment ID")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "transactions_status Accepted Customer Profile."),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{paymentId}/status", method = RequestMethod.GET)
    public String getPaymentInitiationStatus(@PathVariable String paymentId) {
        return TransactionStatus.RCVD.getName();
    }

    @ApiOperation(value = "Get information  about all payments ", notes = "the payment ID")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "transactions_status ?????"),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{paymentId}", method = RequestMethod.GET)
    public Transaction getPaymentInitiation(@PathVariable String paymentId) {

        // TODO according task PIS_01_02. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/10
        return new Transaction();
    }

    private PaymentInitialisationResponse createResponse() {

        // TODO according task PIS_01_02. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/10
        PaymentInitialisationResponse response = new PaymentInitialisationResponse();
        //	return new Resource<>(transactions,
        //             linkTo(methodOn(PaymentInitiationController.class).paymentInitiation(transactions.getTransaction_id())).withSelfRel());

        return response;
    }

}

