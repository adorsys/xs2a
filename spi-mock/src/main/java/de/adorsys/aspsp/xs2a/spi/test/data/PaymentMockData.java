package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;

public class PaymentMockData {

    public static SpiTransactionStatus getPaymentStatusById(String paymentId) {
        SpiPaymentInitiation spiPaymentInitiation = consentMap.get(paymentId);
        if (spiPaymentInitiation !=null) {
            return spiPaymentInitiation.getSpiTransactionStatus();
        }
        return null;
    }
}
