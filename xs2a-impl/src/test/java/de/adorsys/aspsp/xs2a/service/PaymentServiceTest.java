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

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.payment.PaymentValidationService;
import de.adorsys.aspsp.xs2a.service.payment.ReadPaymentFactory;
import de.adorsys.aspsp.xs2a.service.payment.ReadSinglePayment;
import de.adorsys.aspsp.xs2a.service.payment.ScaPaymentService;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    private final byte[] ASPSP_CONSENT_DATA = "ewogIHBheW1lbnRUb2tlbjogQUJDRDEyMzE0MSwKICBzeXN0ZW1JZDogREVEQUlKRUosCiAgbXVsdGl1c2U6IHRydWUsCiAgZXhwaXJlczogMCwKICB0cmFuc2FjdGlvbnM6IFsKICAgIHsKICAgICAgdHJhbnNhY3Rpb25JZDogaWppZWpmaWUyM3IyLAogICAgICBzdGF0dXM6IE9LCiAgICB9LAogICAgewogICAgICB0cmFuc2FjdGlvbklkOiBpamllamZ3cndpZTIzcjIsCiAgICAgIHN0YXR1czogRkFJTEVECiAgICB9LAogICAgewogICAgICB0cmFuc2FjdGlvbklkOiBpamllcnQyamZpZTIzcjIsCiAgICAgIHN0YXR1czogT0sKICAgIH0sCiAgICB7CiAgICAgIHRyYW5zYWN0aW9uSWQ6IGlqMzI0MzJpZWpmaWUyM3IyLAogICAgICBzdGF0dXM6IE9LCiAgICB9CiAgXQp9Cg==".getBytes();


    private final PeriodicPayment PERIODIC_PAYMENT_OK = getPeriodicPayment(IBAN, AMOUNT);
    private final PeriodicPayment PERIODIC_PAYMENT_NOK_IBAN = getPeriodicPayment(WRONG_IBAN, AMOUNT);
    private final PeriodicPayment PERIODIC_PAYMENT_NOK_AMOUNT = getPeriodicPayment(IBAN, EXCESSIVE_AMOUNT);

    private final SinglePayments SINGLE_PAYMENT_OK = getSinglePayment(IBAN, AMOUNT);
    private final SinglePayments SINGLE_PAYMENT_NOK_IBAN = getSinglePayment(WRONG_IBAN, AMOUNT);
    private final SinglePayments SINGLE_PAYMENT_NOK_AMOUNT = getSinglePayment(IBAN, EXCESSIVE_AMOUNT);

    @InjectMocks
    private PaymentService paymentService;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PaymentValidationService validationService;
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
        when(paymentSpi.getPaymentStatusById(PAYMENT_ID, ALLOWED_PAYMENT_PRODUCT, ASPSP_CONSENT_DATA))
            .thenReturn(new SpiResponse<>(SpiTransactionStatus.ACCP, ASPSP_CONSENT_DATA));
        when(paymentSpi.getPaymentStatusById(WRONG_PAYMENT_ID, ALLOWED_PAYMENT_PRODUCT, ASPSP_CONSENT_DATA))
            .thenReturn(new SpiResponse<>(null, ASPSP_CONSENT_DATA));

        //Validation
        when(validationService.validateSinglePayment(SINGLE_PAYMENT_OK, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(Optional.empty());
        when(validationService.validateSinglePayment(SINGLE_PAYMENT_NOK_IBAN, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(Optional.of(RESOURCE_UNKNOWN_400));
        when(validationService.validateSinglePayment(SINGLE_PAYMENT_NOK_AMOUNT, ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(Optional.empty());

        //ScaPayService
        when(scaPaymentService.createSinglePayment(SINGLE_PAYMENT_OK))
            .thenReturn(Optional.of(getPaymentResponse(RCVD, null)));
        when(scaPaymentService.createSinglePayment(SINGLE_PAYMENT_NOK_AMOUNT))
            .thenReturn(Optional.empty());

        when(scaPaymentService.createBulkPayment(Arrays.asList(SINGLE_PAYMENT_OK, SINGLE_PAYMENT_OK)))
            .thenReturn(getBulkResponses(getPaymentResponse(RCVD, null), getPaymentResponse(RCVD, null)));
        when(scaPaymentService.createBulkPayment(Arrays.asList(SINGLE_PAYMENT_OK)))
            .thenReturn(getBulkResponses(getPaymentResponse(RCVD, null)));
        when(scaPaymentService.createBulkPayment(Arrays.asList(SINGLE_PAYMENT_OK, SINGLE_PAYMENT_NOK_AMOUNT)))
            .thenReturn(getBulkResponses(getPaymentResponse(RCVD, null), getPaymentResponse(RJCT, PAYMENT_FAILED)));
    }

    // TODO Update tests after rearranging order of payment creation with pis consent https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/159
    //GetStatus Tests
    @Test
    public void getPaymentStatusById() {
        //When
        ResponseObject<TransactionStatus> response = paymentService.getPaymentStatusById(PAYMENT_ID, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(TransactionStatus.ACCP);
    }

    @Test
    public void getPaymentStatusById_Failure() {
        //When
        ResponseObject<TransactionStatus> response = paymentService.getPaymentStatusById(WRONG_PAYMENT_ID, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(MessageErrorCode.RESOURCE_UNKNOWN_403);
        assertThat(response.getError().getTransactionStatus()).isEqualTo(RJCT);
    }

    //PeriodicPayment Tests
    @Test
    public void initiatePeriodicPayment() {
        when(validationService.validatePeriodicPayment(PERIODIC_PAYMENT_OK, ALLOWED_PAYMENT_PRODUCT)).thenReturn(Optional.empty());
        when(scaPaymentService.createPeriodicPayment(PERIODIC_PAYMENT_OK)).thenReturn(Optional.of(getPaymentResponse(RCVD, null)));
        PeriodicPayment payment = PERIODIC_PAYMENT_OK;
        //When
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.initiatePeriodicPayment(payment, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RCVD);
    }

    @Test
    public void initiatePeriodicPayment_Failure_Validation() {
        when(validationService.validatePeriodicPayment(PERIODIC_PAYMENT_NOK_IBAN, ALLOWED_PAYMENT_PRODUCT)).thenReturn(Optional.of(RESOURCE_UNKNOWN_400));
        PeriodicPayment payment = PERIODIC_PAYMENT_NOK_IBAN;
        initiatePeriodicPaymentFailureTest(payment, RESOURCE_UNKNOWN_400);
    }

    @Test
    public void initiatePeriodicPayment_Failure_ASPSP_RJCT() {
        when(validationService.validatePeriodicPayment(PERIODIC_PAYMENT_NOK_AMOUNT, ALLOWED_PAYMENT_PRODUCT)).thenReturn(Optional.empty());
        when(scaPaymentService.createPeriodicPayment(PERIODIC_PAYMENT_NOK_AMOUNT)).thenReturn(Optional.empty());
        PeriodicPayment payment = PERIODIC_PAYMENT_NOK_AMOUNT;
        initiatePeriodicPaymentFailureTest(payment, PAYMENT_FAILED);
    }

    private void initiatePeriodicPaymentFailureTest(PeriodicPayment payment, MessageErrorCode errorCode) {
        //When
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.initiatePeriodicPayment(payment, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getCode()).isEqualTo(errorCode);
        assertThat(actualResponse.getError().getTransactionStatus()).isEqualTo(RJCT);
    }

    //Bulk Tests
    @Test
    public void createBulkPayments() {
        List<SinglePayments> payment = Arrays.asList(SINGLE_PAYMENT_OK, SINGLE_PAYMENT_OK);
        //When
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payment, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().get(0).getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().get(1).getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().get(0).getTransactionStatus()).isEqualTo(RCVD);
        assertThat(actualResponse.getBody().get(1).getTransactionStatus()).isEqualTo(RCVD);
    }

    @Test
    public void createBulkPayments_Partial_Failure_Validation() {
        List<SinglePayments> payment = Arrays.asList(SINGLE_PAYMENT_NOK_IBAN, SINGLE_PAYMENT_OK);
        createBulkPartialFailureTest(payment, RESOURCE_UNKNOWN_400);
    }

    @Test
    public void createBulkPayments_Partial_Failure_ASPSP_RJCT() {
        List<SinglePayments> payment = Arrays.asList(SINGLE_PAYMENT_OK, SINGLE_PAYMENT_NOK_AMOUNT);
        createBulkPartialFailureTest(payment, PAYMENT_FAILED);
    }

    private void createBulkPartialFailureTest(List<SinglePayments> payment, MessageErrorCode errorCode) {
        //When
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payment, ALLOWED_PAYMENT_PRODUCT);
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
        List<SinglePayments> payment = null;
        createBulkFailureTest(payment, FORMAT_ERROR);
    }

    @Test
    public void createBulkPayments_Failure_Validation() {
        List<SinglePayments> payment = Arrays.asList(SINGLE_PAYMENT_NOK_IBAN, SINGLE_PAYMENT_NOK_IBAN);
        createBulkFailureTest(payment, PAYMENT_FAILED);
    }

    @Test
    public void createBulkPayments_Failure_ASPSP() {
        List<SinglePayments> payment = Arrays.asList(SINGLE_PAYMENT_NOK_AMOUNT, SINGLE_PAYMENT_NOK_AMOUNT);
        createBulkFailureTest(payment, PAYMENT_FAILED);
    }

    private void createBulkFailureTest(List<SinglePayments> payment, MessageErrorCode errorCode) {
        //When
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payment, ALLOWED_PAYMENT_PRODUCT);
        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getCode()).isEqualTo(errorCode);
        assertThat(actualResponse.getError().getTransactionStatus()).isEqualTo(RJCT);
    }

    //SinglePayment tests
    @Test
    public void createPaymentInitiation() {
        SinglePayments payment = SINGLE_PAYMENT_OK;
        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, ALLOWED_PAYMENT_PRODUCT);
        //Then:
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RCVD);
    }

    @Test
    public void createPaymentInitiation_Failure_Validation() {
        SinglePayments payment = SINGLE_PAYMENT_NOK_IBAN;
        createPaymentInitiationFailureTests(payment, RESOURCE_UNKNOWN_400);
    }

    @Test
    public void createPaymentInitiation_Failure_ASPSP_RJCT() {
        SinglePayments payment = SINGLE_PAYMENT_NOK_AMOUNT;
        createPaymentInitiationFailureTests(payment, PAYMENT_FAILED);
    }

    private void createPaymentInitiationFailureTests(SinglePayments payment, MessageErrorCode errorCode) {
        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, ALLOWED_PAYMENT_PRODUCT);
        //Then:
        assertThat(actualResponse.getBody()).isNull();
        assertThat(actualResponse.getError().getTppMessage().getCode()).isEqualTo(errorCode);
        assertThat(actualResponse.getError().getTransactionStatus()).isEqualTo(RJCT);
    }

    @Test
    public void getPaymentById() {
        when(readPaymentFactory.getService((any()))).thenReturn(readSinglePayment);
        when(readSinglePayment.getPayment(any(), anyString())).thenReturn(getSinglePayment(IBAN, "10"));
        //When
        ResponseObject<Object> response = paymentService.getPaymentById(SINGLE, ALLOWED_PAYMENT_PRODUCT, PAYMENT_ID);
        //Than
        assertThat(response.hasError()).isFalse();
        assertThat(response.getError()).isNull();
        assertThat(response.getBody()).isNotNull();
        SinglePayments payment = (SinglePayments) response.getBody();
        assertThat(payment.getEndToEndIdentification()).isEqualTo(PAYMENT_ID);
    }

    @Test
    public void getPaymentById_Failure_wrong_id() {
        when(readPaymentFactory.getService((any()))).thenReturn(readSinglePayment);
        when(readSinglePayment.getPayment(any(), anyString())).thenReturn(null);

        //When
        ResponseObject<Object> response = paymentService.getPaymentById(SINGLE, ALLOWED_PAYMENT_PRODUCT, WRONG_PAYMENT_ID);
        //Than
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getTppMessage().getCode()).isEqualTo(RESOURCE_UNKNOWN_403);
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

    private SinglePayments getSinglePayment(String iban, String amountToPay) {
        SinglePayments singlePayments = new SinglePayments();
        singlePayments.setEndToEndIdentification(PAYMENT_ID);
        Amount amount = new Amount();
        amount.setCurrency(CURRENCY);
        amount.setContent(amountToPay);
        singlePayments.setInstructedAmount(amount);
        singlePayments.setDebtorAccount(getReference(iban));
        singlePayments.setCreditorAccount(getReference(iban));
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
        return payment;
    }

    private List<PaymentInitialisationResponse> getBulkResponses(PaymentInitialisationResponse... response) {
        return Arrays.stream(response).collect(Collectors.toList());
    }
}
