package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.PaymentMockData;

public class PaymentSpiImpl implements PaymentSpi {

    @Override
    public SpiTransactionStatus getPaymentStatusById(String paymentId) {
        return PaymentMockData.getPaymentStatusById(paymentId);
    }

    @Override
    public String createPaymentInitiation(SpiSinglePayments paymentInitiationRequest, boolean tppRedirectPreferred) {
        return PaymentMockData.createPaymentInitiation(paymentInitiationRequest, tppRedirectPreferred);
    }
}
