package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.PaymentMockData;

import java.util.List;

public class PaymentSpiImpl implements PaymentSpi {
    public SpiPaymentInitiation createBulkPayments(List<SpiSinglePayment> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return PaymentMockData.createMultiplePayments(payments,paymentProduct,tppRedirectPreferred);
    }
}
