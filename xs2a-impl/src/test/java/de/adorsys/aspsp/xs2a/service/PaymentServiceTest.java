/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.payment.ReadPaymentFactory;
import de.adorsys.aspsp.xs2a.service.payment.ReadSinglePayment;
import de.adorsys.aspsp.xs2a.service.payment.ScaPaymentService;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.aspsp.xs2a.domain.TransactionStatus.RCVD;
import static de.adorsys.aspsp.xs2a.domain.TransactionStatus.RJCT;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

    private static final String PAYMENT_ID = "12345";
    private static final String WRONG_PAYMENT_ID = "";
    private static final String IBAN = "DE123456789";
    private static final String WRONG_IBAN = "wrong_iban";
    private static final String AMOUNT = "100";
    private static final String EXCESSIVE_AMOUNT = "10000";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final String ALLOWED_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData("zzzzzzzzzzzzzz".getBytes());
    private static final TppInfo TPP_INFO = getTppInfo();
    private static final String TPP_INFO_STR = "tpp info";

    private final PeriodicPayment PERIODIC_PAYMENT_OK = getPeriodicPayment(IBAN, AMOUNT);
    private final PeriodicPayment PERIODIC_PAYMENT_NOK_IBAN = getPeriodicPayment(WRONG_IBAN, AMOUNT);
    private final PeriodicPayment PERIODIC_PAYMENT_NOK_AMOUNT = getPeriodicPayment(IBAN, EXCESSIVE_AMOUNT);

    private final SinglePayment SINGLE_PAYMENT_OK = getSinglePayment(IBAN, AMOUNT);
    private final SinglePayment SINGLE_PAYMENT_NOK_IBAN = getSinglePayment(WRONG_IBAN, AMOUNT);
    private final SinglePayment SINGLE_PAYMENT_NOK_AMOUNT = getSinglePayment(IBAN, EXCESSIVE_AMOUNT);

    @InjectMocks
    private PaymentService paymentService;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private ScaPaymentService scaPaymentService;
    @Mock
    private PaymentSpi paymentSpi;
    @Mock
    private ReadPaymentFactory readPaymentFactory;
    @Mock
    private ReadSinglePayment readSinglePayment;

    @Before
    public void setUp() {
        //Mapper
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.RCVD)).thenReturn(RCVD);
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.ACCP)).thenReturn(TransactionStatus.ACCP);
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.RJCT)).thenReturn(TransactionStatus.RJCT);
        when(paymentMapper.mapToTransactionStatus(null)).thenReturn(null);
        when(paymentMapper.mapToPaymentInitResponseFailedPayment(SINGLE_PAYMENT_NOK_IBAN, RESOURCE_UNKNOWN_400))
            .thenReturn(Optional.of(getPaymentResponse(RJCT, RESOURCE_UNKNOWN_400)));

        //Status by ID
        when(paymentSpi.getPaymentStatusById(PAYMENT_ID, SpiPaymentType.SINGLE, ASPSP_CONSENT_DATA))
            .thenReturn(new SpiResponse<>(SpiTransactionStatus.ACCP, ASPSP_CONSENT_DATA));
        when(paymentSpi.getPaymentStatusById(WRONG_PAYMENT_ID, SpiPaymentType.SINGLE, ASPSP_CONSENT_DATA))
            .thenReturn(new SpiResponse<>(null, ASPSP_CONSENT_DATA));

        //ScaPayService
        when(scaPaymentService.createSinglePayment(SINGLE_PAYMENT_OK, TPP_INFO, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(Optional.of(getPaymentResponse(RCVD, null)));
        when(scaPaymentService.createSinglePayment(SINGLE_PAYMENT_NOK_AMOUNT, TPP_INFO, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(Optional.empty());

        when(scaPaymentService.createBulkPayment(Arrays.asList(SINGLE_PAYMENT_OK, SINGLE_PAYMENT_OK), TPP_INFO, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(getBulkResponses(getPaymentResponse(RCVD, null), getPaymentResponse(RCVD, null)));
        when(scaPaymentService.createBulkPayment(Arrays.asList(SINGLE_PAYMENT_OK), TPP_INFO, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(getBulkResponses(getPaymentResponse(RCVD, null)));
        when(scaPaymentService.createBulkPayment(Arrays.asList(SINGLE_PAYMENT_OK, SINGLE_PAYMENT_NOK_AMOUNT), TPP_INFO, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(getBulkResponses(getPaymentResponse(RCVD, null), getPaymentResponse(RJCT, PAYMENT_FAILED)));
        when(paymentMapper.mapToTppInfo(TPP_INFO_STR))
            .thenReturn(TPP_INFO);
    }

    // TODO Update tests after rearranging order of payment creation with pis consent https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/159
    //GetStatus Tests
    @Test
    public void getPaymentStatusById() {
        //When
        ResponseObject<TransactionStatusResponse> response = paymentService.getPaymentStatusById(PAYMENT_ID, PaymentType.SINGLE);
        //Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(new TransactionStatusResponse(TransactionStatus.ACCP));
    }

    @Test
    public void getPaymentStatusById_Failure() {
        //When
        ResponseObject<TransactionStatusResponse> response = paymentService.getPaymentStatusById(WRONG_PAYMENT_ID, PaymentType.SINGLE);
        //Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.RESOURCE_UNKNOWN_403);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(RJCT);
    }

    //PeriodicPayment Tests
    @Test
    public void initiatePeriodicPayment() {
        when(scaPaymentService.createPeriodicPayment(PERIODIC_PAYMENT_OK, TPP_INFO, ALLOWED_PAYMENT_PRODUCT)).thenReturn(Optional.of(getPaymentResponse(RCVD, null)));
        PeriodicPayment payment = PERIODIC_PAYMENT_OK;
        //When
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.initiatePeriodicPayment(payment, TPP_INFO_STR, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RCVD);
    }

    @Test
    public void initiatePeriodicPayment_Failure_ASPSP_RJCT() {
        when(scaPaymentService.createPeriodicPayment(PERIODIC_PAYMENT_NOK_AMOUNT, TPP_INFO, ALLOWED_PAYMENT_PRODUCT)).thenReturn(Optional.empty());
        PeriodicPayment payment = PERIODIC_PAYMENT_NOK_AMOUNT;
        initiatePeriodicPaymentFailureTest(payment, PAYMENT_FAILED);
    }

    private void initiatePeriodicPaymentFailureTest(PeriodicPayment payment, MessageErrorCode errorCode) {
        //When
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.initiatePeriodicPayment(payment, TPP_INFO_STR, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(errorCode);
        assertThat(actualResponse.getError().getTransactionStatus()).isEqualTo(RJCT);
    }

    //Bulk Tests
    @Test
    public void createBulkPayments() {
        List<SinglePayment> payment = Arrays.asList(SINGLE_PAYMENT_OK, SINGLE_PAYMENT_OK);
        //When
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payment, TPP_INFO_STR, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().get(0).getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().get(1).getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().get(0).getTransactionStatus()).isEqualTo(RCVD);
        assertThat(actualResponse.getBody().get(1).getTransactionStatus()).isEqualTo(RCVD);
    }

    @Test
    public void createBulkPayments_Partial_Failure_ASPSP_RJCT() {
        List<SinglePayment> payment = Arrays.asList(SINGLE_PAYMENT_OK, SINGLE_PAYMENT_NOK_AMOUNT);
        createBulkPartialFailureTest(payment, PAYMENT_FAILED);
    }

    private void createBulkPartialFailureTest(List<SinglePayment> payment, MessageErrorCode errorCode) {
        //When
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payment, TPP_INFO_STR, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().get(0).getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().get(1).getPaymentId()).isNullOrEmpty();
        assertThat(actualResponse.getBody().get(0).getTransactionStatus()).isEqualTo(RCVD);
        assertThat(actualResponse.getBody().get(1).getTransactionStatus()).isEqualTo(RJCT);
        assertThat(actualResponse.getBody().get(0).getTppMessages()).isEqualTo(null);
        assertThat(actualResponse.getBody().get(1).getTppMessages()[0]).isEqualTo(errorCode);
    }

    @Test
    public void createBulkPayments_Failure_null_payments() {
        List<SinglePayment> payment = null;
        createBulkFailureTest(payment, FORMAT_ERROR);
    }

    @Test
    public void createBulkPayments_Failure_Validation() {
        List<SinglePayment> payment = Arrays.asList(SINGLE_PAYMENT_NOK_IBAN, SINGLE_PAYMENT_NOK_IBAN);
        createBulkFailureTest(payment, PAYMENT_FAILED);
    }

    @Test
    public void createBulkPayments_Failure_ASPSP() {
        List<SinglePayment> payment = Arrays.asList(SINGLE_PAYMENT_NOK_AMOUNT, SINGLE_PAYMENT_NOK_AMOUNT);
        createBulkFailureTest(payment, PAYMENT_FAILED);
    }

    private void createBulkFailureTest(List<SinglePayment> payment, MessageErrorCode errorCode) {
        //When
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payment, TPP_INFO_STR, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(errorCode);
        assertThat(actualResponse.getError().getTransactionStatus()).isEqualTo(RJCT);
    }

    //SinglePayment tests
    @Test
    public void createPaymentInitiation() {
        SinglePayment payment = SINGLE_PAYMENT_OK;
        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, TPP_INFO_STR, ALLOWED_PAYMENT_PRODUCT);
        //Then:
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RCVD);
    }

    @Test
    public void createPaymentInitiation_Failure_ASPSP_RJCT() {
        SinglePayment payment = SINGLE_PAYMENT_NOK_AMOUNT;
        createPaymentInitiationFailureTests(payment, PAYMENT_FAILED);
    }

    private void createPaymentInitiationFailureTests(SinglePayment payment, MessageErrorCode errorCode) {
        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, TPP_INFO_STR, ALLOWED_PAYMENT_PRODUCT);
        //Then:
        assertThat(actualResponse.getBody()).isNull();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(errorCode);
        assertThat(actualResponse.getError().getTransactionStatus()).isEqualTo(RJCT);
    }

    @Test
    public void getPaymentById() {
        when(readPaymentFactory.getService((any()))).thenReturn(readSinglePayment);
        when(readSinglePayment.getPayment(anyString())).thenReturn(getSinglePayment(IBAN, "10"));
        //When
        ResponseObject<Object> response = paymentService.getPaymentById(SINGLE, PAYMENT_ID);
        //Than
        assertThat(response.hasError()).isFalse();
        assertThat(response.getError()).isNull();
        assertThat(response.getBody()).isNotNull();
        SinglePayment payment = (SinglePayment) response.getBody();
        assertThat(payment.getEndToEndIdentification()).isEqualTo(PAYMENT_ID);
    }

    @Test
    public void getPaymentById_Failure_wrong_id() {
        when(readPaymentFactory.getService((any()))).thenReturn(readSinglePayment);
        when(readSinglePayment.getPayment(anyString())).thenReturn(null);

        //When
        ResponseObject<Object> response = paymentService.getPaymentById(SINGLE, WRONG_PAYMENT_ID);
        //Than
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_403);
    }

    //Test additional methods
    private PaymentInitialisationResponse getPaymentResponse(TransactionStatus status, MessageErrorCode errorCode) {
        PaymentInitialisationResponse paymentInitialisationResponse = new PaymentInitialisationResponse();
        paymentInitialisationResponse.setTransactionStatus(status);

        paymentInitialisationResponse.setPaymentId(status == RJCT ? null : PAYMENT_ID);
        if (status == RJCT) {
            paymentInitialisationResponse.setTppMessages(new MessageErrorCode[]{errorCode});
        }
        return paymentInitialisationResponse;
    }

    private SinglePayment getSinglePayment(String iban, String amountToPay) {
        SinglePayment singlePayments = new SinglePayment();
        singlePayments.setEndToEndIdentification(PAYMENT_ID);
        Amount amount = new Amount();
        amount.setCurrency(CURRENCY);
        amount.setContent(amountToPay);
        singlePayments.setInstructedAmount(amount);
        singlePayments.setDebtorAccount(getReference(iban));
        singlePayments.setCreditorAccount(getReference(iban));
        singlePayments.setRequestedExecutionDate(LocalDate.now());
        singlePayments.setRequestedExecutionTime(LocalDateTime.now());
        return singlePayments;
    }

    private AccountReference getReference(String iban) {
        AccountReference reference = new AccountReference();
        reference.setIban(iban);
        reference.setCurrency(CURRENCY);

        return reference;
    }

    private SpiAccountReference getSpiReference(String iban) {
        return new SpiAccountReference(iban, null, null, null, null, CURRENCY);
    }

    private PeriodicPayment getPeriodicPayment(String iban, String amountToPay) {
        PeriodicPayment payment = new PeriodicPayment();
        Amount amount = new Amount();
        amount.setCurrency(CURRENCY);
        amount.setContent(amountToPay);
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference(iban));
        payment.setCreditorAccount(getReference(iban));
        payment.setStartDate(LocalDate.now());
        payment.setEndDate(LocalDate.now().plusMonths(4));
        payment.setRequestedExecutionDate(LocalDate.now());
        payment.setRequestedExecutionTime(LocalDateTime.now());
        return payment;
    }

    private List<PaymentInitialisationResponse> getBulkResponses(PaymentInitialisationResponse... response) {
        return Arrays.stream(response).collect(Collectors.toList());
    }

    private static TppInfo getTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setRegistrationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRole("tppRole");
        tppInfo.setNationalCompetentAuthority("nationalCompetentAuthority");
        tppInfo.setRedirectUri("redirectUri");
        tppInfo.setNokRedirectUri("nokRedirectUri");
        return tppInfo;
    }
}
