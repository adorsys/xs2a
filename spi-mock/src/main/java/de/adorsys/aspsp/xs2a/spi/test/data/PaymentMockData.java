package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;

import java.util.*;

public class PaymentMockData {

    private static Map<String, SpiPaymentInitiation> paymentMap = new HashMap<>();

    public String createSpiSinglePayment(SpiSinglePayment spiSinglePayment) {
        return "";
    }

    public static SpiPaymentInitiation createMultiplePayments(List<SpiSinglePayment> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return paymentMap.get(createPaymentInitiation());
    }

    public static SpiTransactionStatus getPaymentStatusById(String paymentId) {
        SpiPaymentInitiation spiPaymentInitiation = paymentMap.get(paymentId);
        if (spiPaymentInitiation != null) {
            return spiPaymentInitiation.getSpiTransactionStatus();
        }
        return null;
    }

    public static String createPaymentInitiation() {

        String paymentId = generatePaymentId();
        paymentMap.put(paymentId, new SpiPaymentInitiation(
        SpiTransactionStatus.ACCP,
        paymentId,
        new SpiAmount(Currency.getInstance("EUR"), "0"),
        false,
        new String[]{},
        "psu processing",
        new String[]{"tpp processing"}));

        return paymentId;
    }

    private static String generatePaymentId() {
        return UUID.randomUUID().toString();
    }
}
