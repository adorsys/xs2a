package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.web.PaymentInitiationController;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.adorsys.aspsp.xs2a.domain.TransactionStatus.ACCP;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;


@Service
@AllArgsConstructor
public class PaymentService {
    private String redirectLinkToSource;
    private final MessageService messageService;
    private final PaymentSpi paymentSpi;
    private final PaymentMapper paymentMapper;

    public ResponseObject<Map<String, TransactionStatus>> getPaymentStatusById(String paymentId, PaymentProduct paymentProduct) {
        Map<String, TransactionStatus> paymentStatusResponse = new HashMap<>();
        TransactionStatus transactionStatus = paymentMapper.mapGetPaymentStatusById(paymentSpi.getPaymentStatusById(paymentId, paymentProduct.getCode()));
        paymentStatusResponse.put("transactionStatus", transactionStatus);
        if (transactionStatus == null) {
            return new ResponseObject<>().fail(new MessageError(new TppMessageInformation(ERROR, MessageCode.PRODUCT_UNKNOWN)
                                                                .text(messageService.getMessage(MessageCode.PRODUCT_UNKNOWN.name()))));
        }
        return new ResponseObject<>(paymentStatusResponse);
    }

    public String createPaymentInitiationAndReturnId(SinglePayments paymentInitiationRequest, boolean tppRedirectPreferred) {
        return paymentSpi.createPaymentInitiation(paymentMapper.mapToSpiSinglePayments(paymentInitiationRequest), tppRedirectPreferred);
    }

    public ResponseObject<PaymentInitialisationResponse> initiatePeriodicPayment(String paymentProduct, boolean tppRedirectPreferred, PeriodicPayment periodicPayment) {
        PaymentInitialisationResponse response = paymentMapper.mapFromSpiPaymentInitializationResponse(
        paymentSpi.initiatePeriodicPayment(paymentProduct, tppRedirectPreferred, paymentMapper.mapToSpiPeriodicPayment(periodicPayment)));

        TransactionStatus status = response.getTransactionStatus();

        return ACCP.equals(status)
               ? new ResponseObject<>(response)
               : new ResponseObject<>().fail(new MessageError(status, new TppMessageInformation(ERROR, MessageCode.PAYMENT_FAILED)));
    }

    public ResponseObject<PaymentInitialisationResponse> createBulkPayments(List<SinglePayments> payments, PaymentProduct paymentProduct, boolean tppRedirectPreferred) {

        List<SpiSinglePayments> spiPayments = paymentMapper.mapToSpiSinglePaymentList(payments);
        SpiPaymentInitialisationResponse spiPaymentInitiation = paymentSpi.createBulkPayments(spiPayments, paymentProduct.getCode(), tppRedirectPreferred);
        PaymentInitialisationResponse paymentInitiation = paymentMapper.mapFromSpiPaymentInitializationResponse(spiPaymentInitiation);

        Links links = new Links();
        links.setRedirect(redirectLinkToSource);
        links.setSelf(linkTo(PaymentInitiationController.class, paymentProduct.getCode()).slash(paymentInitiation.getPaymentId()).toString());
        paymentInitiation.set_links(links);

        return new ResponseObject<>(paymentInitiation);
    }
}
