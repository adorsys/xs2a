package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.impl.PaymentSpiImpl;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTest {

    private static final String PAYMENT_ID = "12345";

    @Autowired
    private PaymentService paymentService;
    @MockBean
    private PaymentSpi paymentSpi;
    @MockBean
    private PaymentMapper paymentMapper;


    @Before
    public void setUpPaymentServiceMock() throws IOException {
        when(paymentMapper.mapToSpiSinglePayments(getCreatePaymentInitiationRequestTest()))
        .thenReturn(getSpiPayment());
        when(paymentMapper.mapFromSpiPaymentInitializationResponse(getSpiPaymentResponse()))
        .thenReturn(getPaymentResponse());
        when(paymentSpi.createPaymentInitiationMockServer(getSpiPayment(), PaymentProduct.SCT.getCode(), false))
        .thenReturn(getSpiPaymentResponse());
    }

    @Test
    public void getPaymentStatusById_successesResult() {
        //Given:
        boolean tppRedirectPreferred = false;
        SinglePayments expectedRequest = getCreatePaymentInitiationRequestTest();
        String validAccountConsentsId = paymentService.createPaymentInitiationAndReturnId(expectedRequest, tppRedirectPreferred);
        TransactionStatus expectedStatus = TransactionStatus.ACCP;

        //When:
        ResponseObject<Map<String, TransactionStatus>> actualStatus = paymentService.getPaymentStatusById(validAccountConsentsId, PaymentProduct.SCT);

        //Then:
        assertThat(actualStatus.getBody()).isNotNull();
        assertThat(actualStatus.getBody().get("transactionStatus")).isEqualTo(expectedStatus);
    }

    @Test
    public void getPaymentStatusById_wrongId() {
        //Given:
        String wrongId = "111111";

        //When:
        ResponseObject<Map<String, TransactionStatus>> actualStatus = paymentService.getPaymentStatusById(wrongId, PaymentProduct.SCT);

        //Then:
        assertThat(actualStatus.getBody()).isNull();
        assertThat(actualStatus.getError().getTppMessage().getCode()).isEqualTo(MessageCode.PRODUCT_UNKNOWN);
    }

    @Test
    public void createBulkPayments() {
        // Given
        List<SinglePayments> payments = Collections.singletonList(getCreatePaymentInitiationRequestTest());
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;

        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createBulkPayments(payments, paymentProduct, tppRedirectPreferred);

        //Then:
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.ACCP);
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

    private SpiPaymentInitialisationResponse getSpiPaymentResponse(){
        SpiPaymentInitialisationResponse spiPaymentInitialisationResponse = new SpiPaymentInitialisationResponse();
        spiPaymentInitialisationResponse.setTransactionStatus(SpiTransactionStatus.RCVD);
        spiPaymentInitialisationResponse.setPaymentId(PAYMENT_ID);
        return  spiPaymentInitialisationResponse;
    }

    private PaymentInitialisationResponse getPaymentResponse(){
        PaymentInitialisationResponse paymentInitialisationResponse = new PaymentInitialisationResponse();
        paymentInitialisationResponse.setTransactionStatus(TransactionStatus.RCVD);
        paymentInitialisationResponse.setPaymentId(PAYMENT_ID);
        return paymentInitialisationResponse;
    }
}
