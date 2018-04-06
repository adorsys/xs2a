package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.PRODUCT_UNKNOWN;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;

@Service
public class PaymentService {
    private MessageService messageService;
    private PaymentSpi paymentSpi;
    private PaymentMapper paymentMapper;

    @Autowired
    public PaymentService(PaymentSpi paymentSpi, PaymentMapper paymentMapper, MessageService messageService) {
        this.paymentSpi = paymentSpi;
        this.paymentMapper = paymentMapper;
        this.messageService = messageService;
    }

    public ResponseObject<Map<String, TransactionStatus>> getPaymentStatusById(String paymentId) {
        Map<String, TransactionStatus> paymentStatusResponse = new HashMap<>();
        TransactionStatus transactionStatus = paymentMapper.mapGetPaymentStatusById(paymentSpi.getPaymentStatusById(paymentId));
        paymentStatusResponse.put("transactionStatus", transactionStatus);
        if (transactionStatus==null) {
            return new ResponseObject<>(new MessageError(new TppMessageInformation(ERROR, PRODUCT_UNKNOWN)
            .text(messageService.getMessage(PRODUCT_UNKNOWN.name()))));
        }
        return new ResponseObject<>(paymentStatusResponse);
    }

    public String createPaymentInitiationAndReturnId(SinglePayments paymentInitiationRequest, boolean tppRedirectPreferred) {
        return paymentSpi.createPaymentInitiation(paymentMapper.mapSinlePayments(paymentInitiationRequest), tppRedirectPreferred);
    }
}
