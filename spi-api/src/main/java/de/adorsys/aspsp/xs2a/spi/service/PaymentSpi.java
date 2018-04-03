package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;

public interface PaymentSpi {

    SpiTransactionStatus getPaymentStatusById(String paymentId);

}
