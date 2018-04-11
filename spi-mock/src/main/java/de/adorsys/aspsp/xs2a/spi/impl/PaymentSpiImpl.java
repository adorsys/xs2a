package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.PaymentMockData;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentSpiImpl implements PaymentSpi {

    @Override
    public SpiTransactionStatus getPaymentStatusById(String paymentId) {
        return PaymentMockData.getPaymentStatusById(paymentId);
    }

    @Override
    public String createPaymentInitiation(SpiSinglePayments spiSinglePayments, String givenPaymentId, boolean tppRedirectPreferred) {
        return PaymentMockData.createPaymentInitiation(spiSinglePayments, givenPaymentId, tppRedirectPreferred);
    }

    @Override
    public SpiPaymentInitialisationResponse initiatePeriodicPayment(String paymentProduct, boolean tppRedirectPreferred, SpiPeriodicPayment periodicPayment) {
        SpiPaymentInitialisationResponse response = new SpiPaymentInitialisationResponse();
        response.setTransactionStatus(SpiTransactionStatus.valueOf(resolveTransactionStatus(periodicPayment)));

        return response;
    }

    private String resolveTransactionStatus(SpiPeriodicPayment payment) {
        Map<String, SpiAccountDetails> map = AccountMockData.getAccountsHashMap();
        boolean isPresent = map.entrySet().stream()
                                    .anyMatch(a -> a.getValue().getIban()
                                                           .equals(payment.getCreditorAccount().getIban()));
        return isPresent ? "ACCP" : "RJCT";
    }

}
