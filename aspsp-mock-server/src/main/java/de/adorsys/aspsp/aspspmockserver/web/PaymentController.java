package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.HttpStatus.CREATED;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/payments")
public class PaymentController {
    private PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(path = "/{payment-product}")
    public ResponseEntity<SpiSinglePayments> createPayment(
    @RequestBody SpiSinglePayments payment) throws Exception {
        return paymentService.addPayment(payment)
        .map(saved -> new ResponseEntity<>(saved, CREATED))
        .orElse(ResponseEntity.badRequest().build());
    }
}
