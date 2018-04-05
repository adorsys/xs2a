package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitiation;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import de.adorsys.aspsp.xs2a.web.PaymentInitiationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Service
public class PaymentService {
    private String redirectLinkToSource;
    private PaymentSpi paymentSpi;
    private PaymentMapper paymentMapper;

    @Autowired
    public PaymentService(PaymentSpi paymentSpi, PaymentMapper paymentMapper,String redirectLinkToSource) {
        this.paymentSpi = paymentSpi;
        this.paymentMapper = paymentMapper;
        this.redirectLinkToSource = redirectLinkToSource;
    }

    public PaymentInitiation createBulkPayments(List<SinglePayments> payments, PaymentProduct paymentProduct, boolean tppRedirectPreferred) {

        List<SpiSinglePayment> spiPayments = paymentMapper.mapToSpiSinglePaymentList(payments);
        SpiPaymentInitiation spiPaymentInitiation = paymentSpi.createBulkPayments(spiPayments, paymentProduct.getName(), tppRedirectPreferred);
        PaymentInitiation paymentInitiation = paymentMapper.mapFromSpiPaymentInitiation(spiPaymentInitiation);

        Links links = new Links();
        links.setRedirect(redirectLinkToSource);
        links.setSelf(linkTo(PaymentInitiationController.class).slash(paymentProduct).slash(paymentInitiation.getPaymentId()).toString());
        paymentInitiation.set_links(links);

        return paymentInitiation;
    }
}
