package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private PaymentSpi paymentSpi;
    private PaymentMapper paymentMapper;

    @Autowired
    public PaymentService(PaymentSpi paymentSpi,PaymentMapper paymentMapper) {
        this.paymentSpi = paymentSpi;
        this.paymentMapper = paymentMapper;
    }

    public TransactionStatus getPaymentStatusById(String paymentId) {
        return paymentMapper.mapGetPaymentStatusById(paymentSpi.getPaymentStatusById(paymentId));
    }

    public String createPaymentInitiationAndReturnId(SinglePayments paymentInitiationRequest, boolean tppRedirectPreferred) {
        return paymentSpi.createPaymentInitiation(paymentMapper.mapSinlePayments(paymentInitiationRequest), tppRedirectPreferred);
    }
}
