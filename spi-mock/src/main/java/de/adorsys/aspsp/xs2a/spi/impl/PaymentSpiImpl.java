package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;
import de.adorsys.aspsp.xs2a.spi.test.data.PaymentMockData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class PaymentSpiImpl implements PaymentSpi {
    @Autowired
    private RestTemplate restTemplate;
    private static final String baseUri = "http://localhost:28080/swagger-ui.html/payment-controller/payment/{payment-product}";

    @Override
    public SpiTransactionStatus getPaymentStatusById(String paymentId, String paymentProduct) {
        return PaymentMockData.getPaymentStatusById(paymentId, paymentProduct);
    }

    @Override
    public String createPaymentInitiation(SpiSinglePayments spiSinglePayments, boolean tppRedirectPreferred) {
        return PaymentMockData.createPaymentInitiation(spiSinglePayments, tppRedirectPreferred);
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

    public SpiPaymentInitialisationResponse createBulkPayments(List<SpiSinglePayments> payments, String paymentProduct, boolean tppRedirectPreferred) {
        return PaymentMockData.createMultiplePayments(payments, paymentProduct, tppRedirectPreferred);
    }

    @Override
    public SpiPaymentInitialisationResponse createPaymentInitiationMockServer(SpiSinglePayments spiSinglePayments, String code, boolean tppRedirectPreferred) {
        ResponseEntity<SpiSinglePayments> responseEntity = restTemplate.exchange(baseUri, HttpMethod.POST, null, new ParameterizedTypeReference<SpiSinglePayments>() {});
        return getResponse(responseEntity,tppRedirectPreferred);
    }

    private SpiPaymentInitialisationResponse getResponse(ResponseEntity<SpiSinglePayments> responseEntity, boolean tppRedirectPreferred) {
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
