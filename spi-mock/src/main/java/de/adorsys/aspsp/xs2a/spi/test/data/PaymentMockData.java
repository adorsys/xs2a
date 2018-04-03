package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;

import java.util.HashMap;
import java.util.Map;

public class PaymentMockData {

    private static Map<String, SpiPaymentInitiation> paymentMap = new HashMap<>();

    public static SpiTransactionStatus getPaymentStatusById(String paymentId) {
        SpiPaymentInitiation spiPaymentInitiation = paymentMap.get(paymentId);
        if (spiPaymentInitiation !=null) {
            return spiPaymentInitiation.getSpiTransactionStatus();
        }
        return null;
    }
}
