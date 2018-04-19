package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.config.RemoteSpiUrls;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;
import de.adorsys.aspsp.xs2a.spi.test.data.PaymentMockData;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.ACCP;
import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.RJCT;

@Component
@AllArgsConstructor
public class PaymentSpiImpl implements PaymentSpi {
    private final RestTemplate restTemplate;
    private final RemoteSpiUrls remoteSpiUrls;


    @Override
    public SpiTransactionStatus getPaymentStatusById(String paymentId, String paymentProduct) {
        return PaymentMockData.getPaymentStatusById(paymentId, paymentProduct);
    }

    @Override
    public SpiPaymentInitialisationResponse initiatePeriodicPayment(String paymentProduct, boolean tppRedirectPreferred, SpiPeriodicPayment periodicPayment) {
        SpiPaymentInitialisationResponse response = new SpiPaymentInitialisationResponse();
        response.setTransactionStatus(resolveTransactionStatus(periodicPayment));

        return response;
    }

    private SpiTransactionStatus resolveTransactionStatus(SpiPeriodicPayment payment) {
        Map<String, SpiAccountDetails> map = AccountMockData.getAccountsHashMap();
        return Optional.of(map.values().stream()
                           .anyMatch(a -> a.getIban()
                                          .equals(payment.getCreditorAccount().getIban())))
               .map(present -> ACCP)
               .orElse(RJCT);
    }

    public SpiPaymentInitialisationResponse createBulkPayments(List<SpiSinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return PaymentMockData.createMultiplePayments(payments, paymentProduct, tppRedirectPreferred);
    }

    @Override
    public SpiPaymentInitialisationResponse createPaymentInitiation(SpiSinglePayments spiSinglePayments, String paymentProduct, boolean tppRedirectPreferred) {
        ResponseEntity<SpiSinglePayments> responseEntity = restTemplate.postForEntity(remoteSpiUrls.getUrl("createPayment"), spiSinglePayments, SpiSinglePayments.class, paymentProduct);
        if (responseEntity.getStatusCode().value() == 201) {
            SpiPaymentInitialisationResponse paymentResponse = new SpiPaymentInitialisationResponse();
            paymentResponse.setTransactionStatus(SpiTransactionStatus.RCVD);
            paymentResponse.setPaymentId(responseEntity.getBody().getPaymentId());
            paymentResponse.setTppRedirectPreferred(tppRedirectPreferred);
            return paymentResponse;
        }
        return null;
    }
}
