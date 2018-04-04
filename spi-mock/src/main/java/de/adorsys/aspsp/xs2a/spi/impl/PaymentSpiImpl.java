package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentSpiImpl implements PaymentSpi {
    @Override
    public SpiPaymentInitialisationResponse initiatePeriodicPayment(String paymentProduct, boolean tppRedirectPreferred, SpiPeriodicPayment periodicPayment) {
        SpiPaymentInitialisationResponse response = new SpiPaymentInitialisationResponse();
        Map<String, SpiAccountDetails> map = AccountMockData.getAccountsHashMap();
        response.setTransaction_status("RJCT");
        map.forEach((k, v) -> {
            if (v.getIban().equals(periodicPayment.getCreditorAccount().getIban())) {
                response.setTransaction_status("ACCP");
            }
        });

        return response;
    }
}
