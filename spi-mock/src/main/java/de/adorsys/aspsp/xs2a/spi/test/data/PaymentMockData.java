package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;

import java.util.*;

public class PaymentMockData {

    private static Map<String, SpiPaymentInitialisationResponse> paymentMap = new HashMap<>();

    public static SpiTransactionStatus getPaymentStatusById(String paymentId) {
        return Optional.ofNullable(paymentMap.get(paymentId))
        .map(SpiPaymentInitialisationResponse::getTransactionStatus)
        .orElse(null);
    }

    public static SpiPaymentInitialisationResponse createMultiplePayments(List<SpiSinglePayment> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return paymentMap.get(createPaymentInitiation(tppRedirectPreferred));
    }

    public static String createPaymentInitiation(boolean tppRedirectPreferred) {
        String paymentId = generatePaymentId();
        SpiPaymentInitialisationResponse response = new SpiPaymentInitialisationResponse();
        response.setTransactionStatus(SpiTransactionStatus.ACCP);
        response.setPaymentId(paymentId);
        response.setScaMethods(null);
        response.setTppRedirectPreferred(tppRedirectPreferred);
        response.setSpiTransactionFees(null);
        response.setPsuMessage(null);
        response.setTppMessages(new String[0]);
        response.setSpiTransactionFeeIndicator(false);
        paymentMap.put(paymentId,response);
        System.out.println("Payment id: " + paymentId);
        return paymentId;
    }

    private static String generatePaymentId() {
        return UUID.randomUUID().toString();
    }
}

