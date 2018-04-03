package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiCreatePaymentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaymentMockData {

    private static Map<String, SpiPaymentInitiation> paymentMap = new HashMap<>();

    public static SpiTransactionStatus getPaymentStatusById(String paymentId) {
        SpiPaymentInitiation spiPaymentInitiation = paymentMap.get(paymentId);
        if (spiPaymentInitiation !=null) {
            return spiPaymentInitiation.getSpiTransactionStatus();
        }
        return null;
    }

    public static String createPaymentInitiation(SpiCreatePaymentRequest pisRequest,
                                              boolean tppRedirectPreferred) {

        String paymentId = generatePaymentId();
        paymentMap.put(paymentId, new SpiPaymentInitiation(
            SpiTransactionStatus.ACCP,
            paymentId,
            pisRequest.getSpiTransactionFees(),
            pisRequest.isSpiTransactionFeeIndicator(),
            pisRequest.getScaMethods(),
            pisRequest.getPsuMessage(),
            pisRequest.getTppMessages(),
            tppRedirectPreferred
            )
        );

        return paymentId;
    }

    private static String generatePaymentId() {
        return UUID.randomUUID().toString();
    }
}
