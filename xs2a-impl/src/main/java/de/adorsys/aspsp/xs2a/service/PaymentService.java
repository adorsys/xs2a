package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitiation;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.web.PaymentInitiationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Service
public class PaymentService {
    private String redirectLinkToSource;
    private PaymentSpi paymentSpi;
    private de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper paymentMapper;

    @Autowired
    public PaymentService(PaymentSpi paymentSpi, PaymentMapper paymentMapper, String redirectLinkToSource) {
        this.paymentSpi = paymentSpi;
        this.paymentMapper = paymentMapper;
        this.redirectLinkToSource = redirectLinkToSource;
    }

    public ResponseObject initiatePeriodicPayment(String paymentProduct, boolean tppRedirectPreferred, PeriodicPayment periodicPayment) {

        PaymentInitialisationResponse response = paymentMapper.mapFromSpiPaymentInitializationResponsepaymentSpi(
        paymentSpi.initiatePeriodicPayment(paymentProduct, tppRedirectPreferred, paymentMapper.mapToSpiPeriodicPayment(periodicPayment)));

        return response == null
               ? new ResponseObject(MessageCode.PAYMENT_FAILED)
               : new ResponseObject<>(response);
    }

    public PaymentInitiation createBulkPayments(List<SinglePayments> payments, PaymentProduct paymentProduct, boolean tppRedirectPreferred) {

        List<SpiSinglePayment> spiPayments = paymentMapper.mapToSpiSinglePaymentList(payments);
        SpiPaymentInitiation spiPaymentInitiation = paymentSpi.createBulkPayments(spiPayments, paymentProduct.getCode(), tppRedirectPreferred);
        PaymentInitiation paymentInitiation = paymentMapper.mapFromSpiPaymentInitiation(spiPaymentInitiation);

        Links links = new Links();
        links.setRedirect(redirectLinkToSource);
        links.setSelf(linkTo(PaymentInitiationController.class).slash(paymentProduct).slash(paymentInitiation.getPaymentId()).toString());
        paymentInitiation.set_links(links);

        return paymentInitiation;
    }
}
