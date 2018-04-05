package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitiation;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private PaymentSpi paymentSpi;
    private PaymentMapper paymentMapper;

    @Autowired
    public PaymentService(PaymentSpi paymentSpi, PaymentMapper paymentMapper) {
        this.paymentSpi = paymentSpi;
        this.paymentMapper = paymentMapper;
    }

    public PaymentInitiation createBulkPayments(List<SinglePayments> payments, PaymentProduct paymentProduct, boolean tppRedirectPreferred) {

        List<SpiSinglePayment> spiPayments = paymentMapper.mapToSpiSinglePaymentList(payments);
        SpiPaymentInitiation bulkPayments = paymentSpi.createBulkPayments(spiPayments, paymentProduct.getName(), tppRedirectPreferred);
        PaymentInitiation paymentInitiation = paymentMapper.mapFromSpiPaymentInitiation(bulkPayments);

        return paymentInitiation;
    }
}
