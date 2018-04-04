package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;

import java.util.List;

public interface PaymentSpi {
    SpiPaymentInitiation createBulkPayments(List<SpiSinglePayment> payments, String  paymentProduct, boolean tppRedirectPreferred);
}
