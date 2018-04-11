package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PaymentMockData {

    private static Map<String, SpiPaymentInitialisationResponse> paymentMap = new HashMap<>();

    public static SpiTransactionStatus getPaymentStatusById(String paymentId) {
        return Optional.ofNullable(paymentMap.get(paymentId))
        .map(SpiPaymentInitialisationResponse::getTransactionStatus)
        .orElse(null);
    }

    public static String createPaymentInitiation(SpiSinglePayments spiSinglePayments, String givenPaymentId, boolean tppRedirectPreferred) {
        String paymentId = givenPaymentId;
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
        return paymentId;
    }

}

