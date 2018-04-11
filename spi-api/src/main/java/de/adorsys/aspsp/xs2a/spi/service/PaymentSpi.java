package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitiation;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;

import java.util.List;

public interface PaymentSpi {

    SpiTransactionStatus getPaymentStatusById(String paymentId);

    String createPaymentInitiation(SpiSinglePayments spiSinglePayments,boolean tppRedirectPreferred);

    SpiPaymentInitialisationResponse initiatePeriodicPayment(String paymentProduct, boolean tppRedirectPreferred, SpiPeriodicPayment periodicPayment);
    SpiPaymentInitiation createBulkPayments(List<SpiSinglePayment> payments, String  paymentProduct, boolean tppRedirectPreferred);
}
