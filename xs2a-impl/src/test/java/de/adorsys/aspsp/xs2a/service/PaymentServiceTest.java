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

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.Xs2aAmount;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.config.factory.ReadPaymentFactory;
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
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RCVD;
import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RJCT;
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
    private final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData();
    private static final TppInfo TPP_INFO = getTppInfo();
    private static final String TPP_INFO_STR = "tpp info";

    private final PeriodicPayment PERIODIC_PAYMENT_OK = getPeriodicPayment(IBAN, AMOUNT);
    private final PeriodicPayment PERIODIC_PAYMENT_NOK_AMOUNT = getPeriodicPayment(IBAN, EXCESSIVE_AMOUNT);

    private final SinglePayment SINGLE_PAYMENT_OK = getSinglePayment(IBAN, AMOUNT);
    private final SinglePayment SINGLE_PAYMENT_NOK_IBAN = getSinglePayment(WRONG_IBAN, AMOUNT);
    private final SinglePayment SINGLE_PAYMENT_NOK_AMOUNT = getSinglePayment(IBAN, EXCESSIVE_AMOUNT);

    private final BulkPayment BULK_PAYMENT_OK = getBulkPayment(SINGLE_PAYMENT_OK, IBAN);
    private final BulkPayment BULK_PAYMENT_NOT_OK = getBulkPayment(SINGLE_PAYMENT_OK, WRONG_IBAN);


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
    @Mock
    private AccountReferenceValidationService referenceValidationService;

    @Before
    public void setUp() {
        //Mapper
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.RCVD)).thenReturn(RCVD);
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.ACCP)).thenReturn(Xs2aTransactionStatus.ACCP);
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.RJCT)).thenReturn(Xs2aTransactionStatus.RJCT);
        when(paymentMapper.mapToTransactionStatus(null)).thenReturn(null);
        when(paymentMapper.mapToSpiPaymentType(PaymentType.SINGLE)).thenReturn(SpiPaymentType.SINGLE);
        when(paymentMapper.mapToSpiPaymentType(PaymentType.PERIODIC)).thenReturn(SpiPaymentType.PERIODIC);
        when(paymentMapper.mapToSpiPaymentType(PaymentType.BULK)).thenReturn(SpiPaymentType.BULK);
        when(paymentMapper.mapToPaymentInitResponseFailedPayment(SINGLE_PAYMENT_NOK_IBAN, RESOURCE_UNKNOWN_400))
            .thenReturn(getPaymentResponse(RJCT, RESOURCE_UNKNOWN_400));

        //Status by ID
        when(paymentSpi.getPaymentStatusById(PAYMENT_ID, SpiPaymentType.SINGLE, ASPSP_CONSENT_DATA))
            .thenReturn(new SpiResponse<>(SpiTransactionStatus.ACCP, ASPSP_CONSENT_DATA));
        when(paymentSpi.getPaymentStatusById(WRONG_PAYMENT_ID, SpiPaymentType.SINGLE, ASPSP_CONSENT_DATA))
            .thenReturn(new SpiResponse<>(null, ASPSP_CONSENT_DATA));

        //ScaPayService
        when(scaPaymentService.createSinglePayment(SINGLE_PAYMENT_OK, TPP_INFO, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(getPaymentResponse(RCVD, null));
        when(scaPaymentService.createSinglePayment(SINGLE_PAYMENT_NOK_AMOUNT, TPP_INFO, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(getPaymentInitiationResponseRJCT());

        when(scaPaymentService.createBulkPayment(BULK_PAYMENT_OK, TPP_INFO, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(getBulkResponses(getPaymentResponse(RCVD, null), getPaymentResponse(RCVD, null)));
        when(scaPaymentService.createBulkPayment(getBulkPayment(SINGLE_PAYMENT_NOK_AMOUNT, WRONG_IBAN), TPP_INFO, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(getBulkResponses(getPaymentResponse(RCVD, null), getPaymentResponse(RJCT, PAYMENT_FAILED)));
        when(paymentMapper.mapToTppInfo(getRequestParameters()))
            .thenReturn(TPP_INFO);

        when(referenceValidationService.validateAccountReferences(any())).thenReturn(ResponseObject.builder().build());
    }

    // TODO Update tests after rearranging order of payment creation with pis consent https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/159
    //GetStatus Tests
    @Test
    public void getPaymentStatusById() {
        //When
        ResponseObject<Xs2aTransactionStatus> response = paymentService.getPaymentStatusById(PAYMENT_ID, PaymentType.SINGLE);
        //Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(Xs2aTransactionStatus.ACCP);
    }

    @Test
    public void getPaymentStatusById_Failure() {
        //When
        ResponseObject<Xs2aTransactionStatus> response = paymentService.getPaymentStatusById(WRONG_PAYMENT_ID, PaymentType.SINGLE);
        //Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.RESOURCE_UNKNOWN_403);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(RJCT);
    }

    //PeriodicPayment Tests
    @Test
    public void initiatePeriodicPayment() {
        when(scaPaymentService.createPeriodicPayment(PERIODIC_PAYMENT_OK, TPP_INFO, ALLOWED_PAYMENT_PRODUCT)).thenReturn(getPaymentResponse(RCVD, null));
        PeriodicPayment payment = PERIODIC_PAYMENT_OK;
        //When
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.initiatePeriodicPayment(payment, TPP_INFO, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RCVD);
    }

    @Test
    public void initiatePeriodicPayment_Failure_ASPSP_RJCT() {
        when(scaPaymentService.createPeriodicPayment(PERIODIC_PAYMENT_NOK_AMOUNT, TPP_INFO, ALLOWED_PAYMENT_PRODUCT)).thenReturn(getPaymentInitiationResponseRJCT());
        PeriodicPayment payment = PERIODIC_PAYMENT_NOK_AMOUNT;
        initiatePeriodicPaymentFailureTest(payment, PAYMENT_FAILED);
    }

    private PaymentInitialisationResponse getPaymentInitiationResponseRJCT() {
        PaymentInitialisationResponse response = new PaymentInitialisationResponse();
        response.setTransactionStatus(RJCT);
        response.setTppMessages(new MessageErrorCode[]{MessageErrorCode.PAYMENT_FAILED});
        return response;
    }

    private void initiatePeriodicPaymentFailureTest(PeriodicPayment payment, MessageErrorCode errorCode) {
        //When
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.initiatePeriodicPayment(payment, TPP_INFO, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RJCT);
        assertThat(actualResponse.getBody().getTppMessages()[0].getName()).isEqualTo(errorCode.getName());
    }

    //Bulk Tests
    @Test
    public void createBulkPayments() {
        BulkPayment payment = BULK_PAYMENT_OK;
        //When
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payment,TPP_INFO, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().get(0).getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().get(1).getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().get(0).getTransactionStatus()).isEqualTo(RCVD);
        assertThat(actualResponse.getBody().get(1).getTransactionStatus()).isEqualTo(RCVD);
    }

    @Test
    public void createBulkPayments_Failure_null_payments() {
        BulkPayment payment = null;
        createBulkFailureTest(payment, FORMAT_ERROR);
    }

    @Test
    public void createBulkPayments_Failure_Validation() {
        BulkPayment payment = BULK_PAYMENT_NOT_OK;
        payment.setPayments(Arrays.asList(SINGLE_PAYMENT_OK, SINGLE_PAYMENT_NOK_AMOUNT));
        createBulkFailureTest(payment, PAYMENT_FAILED);
    }

    private void createBulkFailureTest(BulkPayment payment, MessageErrorCode errorCode) {
        //When
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payment, TPP_INFO, ALLOWED_PAYMENT_PRODUCT);
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
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, TPP_INFO, ALLOWED_PAYMENT_PRODUCT);
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
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, TPP_INFO, ALLOWED_PAYMENT_PRODUCT);
        //Then:
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RJCT);
        assertThat(actualResponse.getBody().getTppMessages()[0].getName()).isEqualTo(errorCode.getName());

    }

    @Test
    public void getPaymentById() {
        when(readPaymentFactory.getService((any()))).thenReturn(readSinglePayment);
        when(readSinglePayment.getPayment(anyString(), anyString())).thenReturn(getSinglePayment(IBAN, "10"));
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
        when(readSinglePayment.getPayment(anyString(), anyString())).thenReturn(null);

        //When
        ResponseObject<Object> response = paymentService.getPaymentById(SINGLE, WRONG_PAYMENT_ID);
        //Than
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_403);
    }

    //Test additional methods
    private PaymentInitialisationResponse getPaymentResponse(Xs2aTransactionStatus status, MessageErrorCode errorCode) {
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
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(CURRENCY);
        amount.setAmount(amountToPay);
        singlePayments.setInstructedAmount(amount);
        singlePayments.setDebtorAccount(getReference(iban));
        singlePayments.setCreditorAccount(getReference(iban));
        singlePayments.setRequestedExecutionDate(LocalDate.now());
        singlePayments.setRequestedExecutionTime(LocalDateTime.now());
        return singlePayments;
    }

    private Xs2aAccountReference getReference(String iban) {
        Xs2aAccountReference reference = new Xs2aAccountReference();
        reference.setIban(iban);
        reference.setCurrency(CURRENCY);

        return reference;
    }

    private SpiAccountReference getSpiReference(String iban) {
        return new SpiAccountReference(iban, null, null, null, null, CURRENCY);
    }

    private PeriodicPayment getPeriodicPayment(String iban, String amountToPay) {
        PeriodicPayment payment = new PeriodicPayment();
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(CURRENCY);
        amount.setAmount(amountToPay);
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

    private BulkPayment getBulkPayment(SinglePayment singlePayment1, String iban) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setPayments(Collections.singletonList(singlePayment1));
        bulkPayment.setRequestedExecutionDate(LocalDate.now());
        bulkPayment.setDebtorAccount(getReference(iban));
        bulkPayment.setBatchBookingPreferred(false);

        return bulkPayment;
    }


    private PaymentRequestParameters getRequestParameters(){
        PaymentRequestParameters requestParameters = new PaymentRequestParameters();

        return requestParameters;
    }
}
