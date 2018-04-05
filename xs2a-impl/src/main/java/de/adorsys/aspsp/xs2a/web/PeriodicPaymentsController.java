package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/periodic-payments")
@Api(value = "api/v1/periodic-payments", tags = "PISP, Payments", description = "Orders for periodic payments")
public class PeriodicPaymentsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
    private PaymentService paymentService;

    @Autowired
    public PeriodicPaymentsController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ApiOperation(value = "The TPP can submit a recurring payment initiation where the starting date, frequency and conditionally an end date is provided. Once authorised by the PSU, the payment then will be executed by the ASPSP, if possible, following this “standing order” as submitted by the TPP.")
    @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{payment-product}", method = RequestMethod.POST)
    public ResponseEntity<PaymentInitialisationResponse> initiationForStandingOrdersForRecurringOrPeriodicPayments(
    @ApiParam(name = "payment-product", value = "Named payment product", example = "sepa-credit-transfers")
    @PathVariable(name = "payment-product", required = true) String paymentProduct,
    @ApiParam()
    @RequestParam(name = "", required = false) boolean tppRedirectPreferred,
    @ApiParam(name = "Periodic Payment", value = "All data relevant for the corresponding payment product and necessary for execution of the standing order.")
    @RequestBody PeriodicPayment periodicPayment
    ) {
        ResponseObject responseObject = paymentService.initiatePeriodicPayment(paymentProduct, tppRedirectPreferred, periodicPayment);
        LOGGER.debug("initiatePerriodicPayment(): response is {}", responseObject.isSuccess() ? "success" : "failure" + periodicPayment.toString());
        HttpStatus httpStatus = responseObject.isSuccess() ? HttpStatus.OK : HttpStatus.valueOf(responseObject.getMessage().getCode());

        return responseObject.isSuccess()
               ? new ResponseEntity<>((PaymentInitialisationResponse) responseObject.getData(), httpStatus)
               : new ResponseEntity<>(httpStatus);
    }

}
