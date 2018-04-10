package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Currency;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTest {
    private final String PERIODIC_PAYMENT_DATA = "/json/PeriodicPaymentTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @Autowired
    private PaymentService paymentService;

    @MockBean(name = "paymentSpi")
    private PaymentSpi paymentSpi;

    @Before
    public void setUp() {
        when(paymentSpi.initiatePeriodicPayment(any(), anyBoolean(), any())).thenReturn(readSpiPaymentInitializationResponse());
    }

    @Test
    public void initiatePeriodicPayment() throws IOException {
        //Given:
        String paymentProdct = "123123";
        boolean tppRedirectPreferred = false;
        PeriodicPayment periodicPayment = readPeriodicPayment();
        ResponseObject<PaymentInitialisationResponse> expectedResult = readResponseObject();

        //When:
        ResponseObject<PaymentInitialisationResponse> result = paymentService.initiatePeriodicPayment(paymentProdct, tppRedirectPreferred, periodicPayment);

        //Than:
        assertThat(result.getError()).isEqualTo(expectedResult.getError());
        assertThat(result.getBody().getTransaction_status().getName()).isEqualTo(expectedResult.getBody().getTransaction_status().getName());
        assertThat(result.getBody().get_links()).isEqualTo(expectedResult.getBody().get_links());
    }

    private ResponseObject<PaymentInitialisationResponse> readResponseObject() {

        return new ResponseObject<>(getPaymentInitializationResponse());
    }

    private PeriodicPayment readPeriodicPayment() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(PERIODIC_PAYMENT_DATA, UTF_8), PeriodicPayment.class);
    }

    private PaymentInitialisationResponse getPaymentInitializationResponse() {
        PaymentInitialisationResponse resp = new PaymentInitialisationResponse();
        resp.setTransaction_status(TransactionStatus.ACCP);
        resp.set_links(new Links());
        return resp;
    }

    private SpiPaymentInitialisationResponse readSpiPaymentInitializationResponse() {
        SpiPaymentInitialisationResponse resp = new SpiPaymentInitialisationResponse();
        resp.setTransactionStatus("ACCP");

        return resp;
    }

    @Test
    public void getPaymentStatusById_successesResult() {
        //Given:
        boolean tppRedirectPreferred = false;
        SinglePayments expectedRequest = getCreatePaymentInitiationRequestTest();
        String validAccountConsentsId = paymentService.createPaymentInitiationAndReturnId(expectedRequest, tppRedirectPreferred);
        TransactionStatus expectedStatus = TransactionStatus.ACCP;

        //When:
        ResponseObject<Map<String, TransactionStatus>> actualStatus = paymentService.getPaymentStatusById(validAccountConsentsId);

        //Then:
        assertThat(actualStatus.getBody()).isNotNull();
        assertThat(actualStatus.getBody().get("transactionStatus")).isEqualTo(expectedStatus);
    }

    private SinglePayments getCreatePaymentInitiationRequestTest() {
        Amount amount = new Amount();
        amount.setCurrency(Currency.getInstance("EUR"));
        AccountReference accountReference = new AccountReference();
        accountReference.setIban("DE23100120020123456789");
        amount.setContent("123.40");
        SinglePayments singlePayments = new SinglePayments();
        singlePayments.setInstructedAmount(amount);
        singlePayments.setDebtorAccount(accountReference);
        singlePayments.setCreditorName("Merchant123");
        singlePayments.setPurposeCode(new PurposeCode("BEQNSD"));
        singlePayments.setCreditorAgent(new BICFI("q1bjkbs1"));
        singlePayments.setCreditorAccount(accountReference);
        singlePayments.setPurposeCode(new PurposeCode("BCENECEQ"));
        singlePayments.setRemittanceInformationUnstructured("Ref Number Merchant");

        return singlePayments;
    }

    @Test
    public void getPaymentStatusById_wrongId() {
        //Given:
        String wrongId = "111111";

        //When:
        ResponseObject<Map<String, TransactionStatus>> actualStatus = paymentService.getPaymentStatusById(wrongId);

        //Then:
        assertThat(actualStatus.getBody()).isNull();
        assertThat(actualStatus.getError().getTppMessage().getCode()).isEqualTo(MessageCode.PRODUCT_UNKNOWN);
    }
}
