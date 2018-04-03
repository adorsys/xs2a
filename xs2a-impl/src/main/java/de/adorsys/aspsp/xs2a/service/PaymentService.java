package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
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

    public PaymentInitialisationResponse getMessage(SinglePayments[] payments, boolean tppRedirectPreferred) {
        return null;
    }
}
