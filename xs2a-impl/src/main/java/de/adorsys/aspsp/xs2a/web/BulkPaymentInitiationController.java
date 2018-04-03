
package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/bulk-payments")
@Api(value = "api/v1/consents", tags = "Bulk payment initiation", description = "Payment Initiation for Bulk Payments and Multiple Payments")
public class BulkPaymentInitiationController {
    private PaymentService paymentService;

    @Autowired
    public BulkPaymentInitiationController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ApiOperation(value = "Creates a bulk payment initiation request at the ASPSP")
    @ApiResponses(value =
                  {@ApiResponse(code = 201, message = "transactions_status received, a list of hyperlinks to be recognized by the Tpp."),
                  @ApiResponse(code = 400, message = "Bad request")
                  })

    @PostMapping("/{payment-product}")
    public PaymentInitialisationResponse createPaymentInitiation(
    @ApiParam(name = "payment-product", value = "The addressed payment product endpoint for bulk payments e.g. for a bulk SEPA Credit Transfers")
    @PathVariable("payment-product") PaymentProduct paymentProduct,
    @ApiParam(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @RequestParam(name = "tppRedirectPreferred", required = false) boolean tppRedirectPreferred,
    @RequestBody SinglePayments[] payments) {

        return paymentService.getMessage(payments, tppRedirectPreferred);
    }
}



