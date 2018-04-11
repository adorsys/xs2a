package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private PaymentSpi paymentSpi;
    private PaymentMapper paymentMapper;

    @Autowired
    public PaymentService(PaymentSpi paymentSpi, PaymentMapper paymentMapper) {
        this.paymentSpi = paymentSpi;
        this.paymentMapper = paymentMapper;
    }

    public ResponseObject initiatePeriodicPayment(String paymentProduct, boolean tppRedirectPreferred, PeriodicPayment periodicPayment) {

        PaymentInitialisationResponse response = paymentMapper.mapFromSpiPaymentInitializationResponse(
        paymentSpi.initiatePeriodicPayment(paymentProduct, tppRedirectPreferred, paymentMapper.mapToSpiPeriodicPayment(periodicPayment)));

        return new ResponseObject<>(response);
    }
}
