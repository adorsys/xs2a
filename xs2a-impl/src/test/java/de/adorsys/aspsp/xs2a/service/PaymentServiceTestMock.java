package de.adorsys.aspsp.xs2a.service;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTestMock {
    private final String PERIODIC_PAYMENT_DATA = "/json/PeriodicPaymentTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private static final String PAYMENT_ID = "12345";


    @Autowired
    private PaymentService paymentService;

    @MockBean(name = "paymentSpi")
    private PaymentSpi paymentSpi;
    @MockBean(name = "paymentMapper")
    private PaymentMapper paymentMapper;


    @Before
    public void setUp() {
        when(paymentSpi.initiatePeriodicPayment(any(), anyBoolean(), any())).thenReturn(readSpiPaymentInitializationResponse());
        when(paymentMapper.mapToSpiSinglePayments(getCreatePaymentInitiationRequestTest()))
        .thenReturn(getSpiPayment());
        when(paymentMapper.mapFromSpiPaymentInitializationResponse(getSpiPaymentResponse()))
        .thenReturn(getPaymentResponse());
        when(paymentSpi.createPaymentInitiationMockServer(getSpiPayment(), PaymentProduct.SCT.getCode(), false))
        .thenReturn(getSpiPaymentResponse());
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
        assertThat(result.getBody().getTransactionStatus().getName()).isEqualTo(expectedResult.getBody().getTransactionStatus().getName());
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
        resp.setTransactionStatus(TransactionStatus.ACCP);
        resp.set_links(new Links());
        return resp;
    }

    private SpiPaymentInitialisationResponse readSpiPaymentInitializationResponse() {
        SpiPaymentInitialisationResponse resp = new SpiPaymentInitialisationResponse();
        resp.setTransactionStatus(SpiTransactionStatus.ACCP);

        return resp;
    }

    @Test
    public void createPaymentInitiation() {
        // Given
        SinglePayments payment = getCreatePaymentInitiationRequestTest();
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;

        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, paymentProduct, tppRedirectPreferred);

        //Then:
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
    }

    private SpiSinglePayments getSpiPayment() {
        SpiSinglePayments spiPayment = new SpiSinglePayments();
        return spiPayment;
    }

    private SpiPaymentInitialisationResponse getSpiPaymentResponse() {
        SpiPaymentInitialisationResponse spiPaymentInitialisationResponse = new SpiPaymentInitialisationResponse();
        spiPaymentInitialisationResponse.setTransactionStatus(SpiTransactionStatus.RCVD);
        spiPaymentInitialisationResponse.setPaymentId(PAYMENT_ID);
        return spiPaymentInitialisationResponse;
    }

    private PaymentInitialisationResponse getPaymentResponse() {
        PaymentInitialisationResponse paymentInitialisationResponse = new PaymentInitialisationResponse();
        paymentInitialisationResponse.setTransactionStatus(TransactionStatus.RCVD);
        paymentInitialisationResponse.setPaymentId(PAYMENT_ID);
        return paymentInitialisationResponse;
    }

    private SinglePayments getCreatePaymentInitiationRequestTest() {
        Amount amount = new Amount();
        amount.setCurrency(Currency.getInstance("EUR"));
        AccountReference accountReference = new AccountReference();
        accountReference.setIban("DE23100120020123456789");
        amount.setContent("123.40");
        BICFI bicfi = new BICFI();
        bicfi.setCode("vnldkvn");
        SinglePayments singlePayments = new SinglePayments();
        singlePayments.setInstructedAmount(amount);
        singlePayments.setDebtorAccount(accountReference);
        singlePayments.setCreditorName("Merchant123");
        singlePayments.setPurposeCode(new PurposeCode("BEQNSD"));
        singlePayments.setCreditorAgent(bicfi);
        singlePayments.setCreditorAccount(accountReference);
        singlePayments.setPurposeCode(new PurposeCode("BCENECEQ"));
        singlePayments.setRemittanceInformationUnstructured("Ref Number Merchant");

        return singlePayments;
    }

}
