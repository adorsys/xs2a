package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PaymentMockData {

    private static Map<String, SpiPaymentInitiation> paymentMap = new HashMap<>();

    public String createSpiSinglePayment(SpiSinglePayment spiSinglePayment){
        return "" ;
    }

    public static SpiPaymentInitiation createMultiplePayments(List<SpiSinglePayment> payments, String paymentProduct, boolean tppRedirectPreferred) {


        return null;
    }

    public static SpiTransactionStatus getPaymentStatusById(String paymentId) {
        SpiPaymentInitiation spiPaymentInitiation = paymentMap.get(paymentId);
        if (spiPaymentInitiation !=null) {
            return spiPaymentInitiation.getSpiTransactionStatus();
        }
        return null;
    }

    public static String createPaymentInitiation(SpiPaymentInitiation pisRequest,
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
