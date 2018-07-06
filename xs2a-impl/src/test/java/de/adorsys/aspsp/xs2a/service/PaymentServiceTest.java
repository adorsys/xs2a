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

import de.adorsys.aspsp.xs2a.service.consent.pis.PisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTest {

    private static final String PAYMENT_ID = "12345";
    private static final String PAYMENT_CONSENT_ID = "12345678";
    private static final String WRONG_PAYMENT_ID = "0";
    private static final String IBAN = "DE123456789";
    private static final String WRONG_IBAN = "wrong_iban";
    private static final String AMOUNT = "100";
    private static final String EXCESSIVE_AMOUNT = "10000";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final LocalDate DATE = LocalDate.now();
    private static final LocalDateTime TIME = LocalDateTime.now();
    private static final String ALLOWED_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String FORBIDDEN_PAYMENT_PRODUCT = "cross-border-credit-transfers";

    @Autowired
    private PaymentService paymentService;
    @Autowired
    PaymentMapper paymentMapper;
    @Autowired
    AccountMapper accountMapper;

    @MockBean(name = "paymentSpi")
    private PaymentSpi paymentSpi;
    @MockBean(name = "accountService")
    private AccountService accountService;

    @MockBean(name = "pisConsentService")
    private PisConsentService pisConsentService;

    @MockBean(name = "aspspProfileService")
    private AspspProfileService aspspProfileService;

   /* @Before
    public void setUp() {
        //SinglePayment
        when(paymentSpi.createPaymentInitiation(paymentMapper.mapToSpiSinglePayments(getPaymentInitiationRequest(IBAN, AMOUNT)), ALLOWED_PAYMENT_PRODUCT, false))
            .thenReturn(getSpiPaymentResponse(RCVD));
        when(paymentSpi.createPaymentInitiation(paymentMapper.mapToSpiSinglePayments(getPaymentInitiationRequest(IBAN, EXCESSIVE_AMOUNT)), ALLOWED_PAYMENT_PRODUCT, false))
            .thenReturn(null);
        //Bulk
        when(paymentSpi.createBulkPayments(paymentMapper.mapToSpiSinglePaymentList(Arrays.asList(getPaymentInitiationRequest(IBAN, AMOUNT), getPaymentInitiationRequest(IBAN, AMOUNT))), ALLOWED_PAYMENT_PRODUCT, false))
            .thenReturn(Arrays.asList(getSpiPaymentResponse(ACCP), getSpiPaymentResponse(ACCP)));
        when(paymentSpi.createBulkPayments(paymentMapper.mapToSpiSinglePaymentList(Arrays.asList(getPaymentInitiationRequest(IBAN, AMOUNT), getPaymentInitiationRequest(WRONG_IBAN, AMOUNT))), ALLOWED_PAYMENT_PRODUCT, false))
            .thenReturn(Arrays.asList(getSpiPaymentResponse(ACCP), getSpiPaymentResponse(ACCP)));
        when(paymentSpi.createBulkPayments(paymentMapper.mapToSpiSinglePaymentList(Arrays.asList(getPaymentInitiationRequest(IBAN, AMOUNT), getPaymentInitiationRequest(IBAN, EXCESSIVE_AMOUNT))), ALLOWED_PAYMENT_PRODUCT, false))
            .thenReturn(Arrays.asList(getSpiPaymentResponse(ACCP), getSpiPaymentResponse(RJCT)));
        when(paymentSpi.createBulkPayments(paymentMapper.mapToSpiSinglePaymentList(Collections.singletonList(getPaymentInitiationRequest(IBAN, AMOUNT))), ALLOWED_PAYMENT_PRODUCT, false))
            .thenReturn(Arrays.asList(getSpiPaymentResponse(ACCP)));

        //Periodic
        when((paymentSpi.initiatePeriodicPayment(paymentMapper.mapToSpiPeriodicPayment(getPeriodicPayment(IBAN, EXCESSIVE_AMOUNT)), ALLOWED_PAYMENT_PRODUCT, false)))
            .thenReturn(null);

        //Status by ID
        when(paymentSpi.getPaymentStatusById(PAYMENT_ID, PaymentProduct.SCT.getCode()))
            .thenReturn(ACCP);
        when(paymentSpi.getPaymentStatusById(WRONG_PAYMENT_ID, PaymentProduct.SCT.getCode()))
            .thenReturn(RJCT);

        //AccountExists
        when(accountService.getAccountDetailsByAccountReference(getReference(IBAN)))
            .thenReturn(Optional.of(getDetails(IBAN)));
        when(accountService.getAccountDetailsByAccountReference(getReference(WRONG_IBAN)))
            .thenReturn(Optional.empty());

        //PaymentProduct PSU check
        when(accountService.isInvalidPaymentProductForPsu(getPaymentInitiationRequest(IBAN, AMOUNT).getDebtorAccount(), ALLOWED_PAYMENT_PRODUCT))
            .thenReturn(false);
        when(accountService.isInvalidPaymentProductForPsu(getPaymentInitiationRequest(IBAN, AMOUNT).getDebtorAccount(), FORBIDDEN_PAYMENT_PRODUCT))
            .thenReturn(true);

        //Consents
        when(pisConsentService.createPisConsentForSinglePaymentAndGetId(any()))
            .thenReturn(PAYMENT_CONSENT_ID);
        when(pisConsentService.createPisConsentForBulkPaymentAndGetId(any()))
            .thenReturn(PAYMENT_CONSENT_ID);
        when(pisConsentService.createPisConsentForPeriodicPaymentAndGetId(any()))
            .thenReturn(PAYMENT_CONSENT_ID);
    }*/

    // TODO Update tests after rearranging order of payment creation with pis consent https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/159
    @Test
    public void getPaymentStatusById_successesResult() {
      /*  //Given
        TransactionStatus expectedTransactionStatus = TransactionStatus.ACCP;
        PaymentProduct paymentProduct = PaymentProduct.SCT;

        //When:
        ResponseObject<TransactionStatus> actualResponse = paymentService.getPaymentStatusById(PAYMENT_ID, paymentProduct.getCode());

        //Then:
        assertThat(actualResponse.getBody()).isEqualTo(expectedTransactionStatus);*/
    }
/*
    @Test
    public void getPaymentStatusById_wrongId() {
        //Given
        TransactionStatus expectedTransactionStatus = TransactionStatus.RJCT;
        PaymentProduct paymentProduct = PaymentProduct.SCT;

        //When:
        ResponseObject<TransactionStatus> actualResponse = paymentService.getPaymentStatusById(WRONG_PAYMENT_ID, paymentProduct.getCode());

        //Then:
        assertThat(actualResponse.getBody()).isEqualTo(expectedTransactionStatus);
    }

    //Bulk Tests
    @Test
    public void createBulkPayments_Success_Complete_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        List<SinglePayments> payments = Arrays.asList(getPaymentInitiationRequest(IBAN, AMOUNT), getPaymentInitiationRequest(IBAN, AMOUNT));
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        //Then
        createBulkSuccess(redirect, payments, paymentProduct);
        createBulkSuccess(oauth, payments, paymentProduct);
    }

    private void createBulkSuccess(boolean redirect, List<SinglePayments> payments, PaymentProduct product) {
        when(aspspProfileService.isRedirectMode()).thenReturn(redirect);
        //When:
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payments, product.getCode(), false);
        //Then:
        assertThat(actualResponse.getBody()).isNotEmpty();
        assertThat(actualResponse.getBody().stream()
                       .map(PaymentInitialisationResponse::getTransactionStatus)
                       .allMatch(t -> t.equals(TransactionStatus.ACCP))).isTrue();
    }

    @Test
    public void createBulkPayments_Success_Partial_wrong_iban_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        List<SinglePayments> payments = Arrays.asList(getPaymentInitiationRequest(IBAN, AMOUNT), getPaymentInitiationRequest(WRONG_IBAN, AMOUNT));
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        MessageErrorCode errorCode = MessageErrorCode.RESOURCE_UNKNOWN_400;
        //Then
        createBulkPartial(redirect, payments, paymentProduct, errorCode);
        createBulkPartial(oauth, payments, paymentProduct, errorCode);
    }

    @Test
    public void createBulkPayments_Success_Partial_null_payment_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        List<SinglePayments> payments = Arrays.asList(getPaymentInitiationRequest(IBAN, AMOUNT), null);
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        MessageErrorCode errorCode = MessageErrorCode.FORMAT_ERROR;
        //Then
        createBulkPartial(redirect, payments, paymentProduct, errorCode);
        createBulkPartial(oauth, payments, paymentProduct, errorCode);
    }

    @Test
    public void createBulkPayments_Success_Partial_payment_rejected_by_ASPSP_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        List<SinglePayments> payments = Arrays.asList(getPaymentInitiationRequest(IBAN, AMOUNT), getPaymentInitiationRequest(IBAN, EXCESSIVE_AMOUNT));
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        MessageErrorCode errorCode = MessageErrorCode.PAYMENT_FAILED;
        //Then
        createBulkPartial(redirect, payments, paymentProduct, errorCode);
        createBulkPartial(oauth, payments, paymentProduct, errorCode);
    }

    private void createBulkPartial(boolean redirect, List<SinglePayments> payments, PaymentProduct product, MessageErrorCode errorCode) {
        when(aspspProfileService.isRedirectMode()).thenReturn(redirect);
        //When:
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payments, product.getCode(), false);
        //Then:
        if (!redirect) {
            assertThat(actualResponse.getBody()).isNotEmpty();
            assertThat(actualResponse.getBody().get(0).getTransactionStatus() == TransactionStatus.ACCP && actualResponse.getBody().get(0).getTppMessages().length == 0).isTrue();
            assertThat(actualResponse.getBody().get(1).getTransactionStatus() == TransactionStatus.RJCT && actualResponse.getBody().get(1).getTppMessages()[0] == errorCode).isTrue();
        }
    }

    @Test
    public void createBulkPayments_Fail_wrong_product_PSU_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        List<SinglePayments> payments = Arrays.asList(getPaymentInitiationRequest(IBAN, AMOUNT), getPaymentInitiationRequest(IBAN, AMOUNT));
        PaymentProduct paymentProduct = PaymentProduct.CBCT;
        MessageErrorCode errorCode = MessageErrorCode.PAYMENT_FAILED;
        //Then
        createBulkTotal_fail(redirect, payments, paymentProduct, errorCode);
        createBulkTotal_fail(oauth, payments, paymentProduct, errorCode);
    }

    @Test
    public void createBulkPayments_Fail_wrong_acc_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        List<SinglePayments> payments = Arrays.asList(getPaymentInitiationRequest(WRONG_IBAN, AMOUNT), getPaymentInitiationRequest(WRONG_IBAN, AMOUNT));
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        MessageErrorCode errorCode = MessageErrorCode.PAYMENT_FAILED;
        //Then
        createBulkTotal_fail(redirect, payments, paymentProduct, errorCode);
        createBulkTotal_fail(oauth, payments, paymentProduct, errorCode);
    }

    @Test
    public void createBulkPayments_Fail_rejected_by_ASPSP_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        List<SinglePayments> payments = Arrays.asList(getPaymentInitiationRequest(IBAN, EXCESSIVE_AMOUNT), getPaymentInitiationRequest(IBAN, EXCESSIVE_AMOUNT));
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        MessageErrorCode errorCode = MessageErrorCode.PAYMENT_FAILED;
        //Then
        createBulkTotal_fail(redirect, payments, paymentProduct, errorCode);
        createBulkTotal_fail(oauth, payments, paymentProduct, errorCode);
    }

    @Test
    public void createBulkPayments_Fail_null_payment_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        List<SinglePayments> payments = Arrays.asList(null, null);
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        MessageErrorCode errorCode = MessageErrorCode.PAYMENT_FAILED;
        //Then
        createBulkTotal_fail(redirect, payments, paymentProduct, errorCode);
        createBulkTotal_fail(oauth, payments, paymentProduct, errorCode);
    }

    private void createBulkTotal_fail(boolean redirect, List<SinglePayments> payments, PaymentProduct product, MessageErrorCode errorCode) {
        when(aspspProfileService.isRedirectMode()).thenReturn(redirect);
        //When:
        ResponseObject<List<PaymentInitialisationResponse>> actualResponse = paymentService.createBulkPayments(payments, product.getCode(), false);
        //Then:
        if (!redirect) {
            assertThat(actualResponse.getBody()).isNullOrEmpty();
            assertThat(actualResponse.getError()).isNotNull();
            assertThat(actualResponse.getError().getTppMessage().getCode()).isEqualTo(errorCode);
        }
    }

    //Periodic Tests
    @Test
    public void initiatePeriodicPayment_Success_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        PeriodicPayment payment = getPeriodicPayment(IBAN, AMOUNT);
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        //Then
        createPeriodic_Success(redirect, payment, paymentProduct);
        createPeriodic_Success(oauth, payment, paymentProduct);
    }

    private void createPeriodic_Success(boolean redirect, PeriodicPayment payment, PaymentProduct product) {
        when(aspspProfileService.isRedirectMode()).thenReturn(redirect);
        when((paymentSpi.initiatePeriodicPayment(any(SpiPeriodicPayment.class), eq(ALLOWED_PAYMENT_PRODUCT), anyBoolean())))
            .thenReturn(getSpiPaymentResponse(ACCP));
        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.initiatePeriodicPayment(payment, product.getCode(), false);
        //Then:
        assertThat(actualResponse.getError()).isNull();
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.ACCP);
        if (!redirect) {
            assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        }
    }

    @Test
    public void createPeriodic_Fail_wrong_acc_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        PeriodicPayment payment = getPeriodicPayment(WRONG_IBAN, AMOUNT);
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        MessageErrorCode errorCode = MessageErrorCode.RESOURCE_UNKNOWN_400;
        //Then
        createPeriodic_Fail(redirect, payment, paymentProduct, errorCode);
        createPeriodic_Fail(oauth, payment, paymentProduct, errorCode);
    }

    @Test
    public void createPeriodic_Fail_wrong_product_invalid_for_PSU_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        PeriodicPayment payment = getPeriodicPayment(IBAN, AMOUNT);
        PaymentProduct paymentProduct = PaymentProduct.CBCT;
        MessageErrorCode errorCode = MessageErrorCode.PRODUCT_INVALID;
        //Then
        createPeriodic_Fail(redirect, payment, paymentProduct, errorCode);
        createPeriodic_Fail(oauth, payment, paymentProduct, errorCode);
    }

    @Test
    public void createPeriodic_Fail_null_payment_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        PeriodicPayment payment = null;
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        MessageErrorCode errorCode = MessageErrorCode.FORMAT_ERROR;
        //Then
        createPeriodic_Fail(redirect, payment, paymentProduct, errorCode);
        createPeriodic_Fail(oauth, payment, paymentProduct, errorCode);
    }

    @Test
    public void createPeriodic_Fail_ASPSP_rejected_redirect_and_oauth() {
        //Given
        boolean redirect = true;
        boolean oauth = false;
        PeriodicPayment payment = getPeriodicPayment(IBAN, EXCESSIVE_AMOUNT);
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        MessageErrorCode errorCode = MessageErrorCode.PAYMENT_FAILED;
        //Then
        createPeriodic_Fail(redirect, payment, paymentProduct, errorCode);
        createPeriodic_Fail(oauth, payment, paymentProduct, errorCode);
    }

    private void createPeriodic_Fail(boolean redirect, PeriodicPayment payment, PaymentProduct product, MessageErrorCode errorCode) {
        when(aspspProfileService.isRedirectMode()).thenReturn(redirect);
        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.initiatePeriodicPayment(payment, product.getCode(), false);
        //Then:
        if (!redirect) {
            assertThat(actualResponse.getBody()).isNull();
            assertThat(actualResponse.getError()).isNotNull();
            assertThat(actualResponse.getError().getTppMessage().getCode()).isEqualTo(errorCode);
        }
    }

    //Single Tests
    @Test
    public void createPaymentInitiation_Success() {
        // Given
        SinglePayments payment = getPaymentInitiationRequest(IBAN, AMOUNT);
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;

        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, paymentProduct.getCode(), tppRedirectPreferred);

        //Then:
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
    }

    @Test
    public void createPaymentInitiation_Failure_nullPayment() {
        // Given
        SinglePayments payment = null;
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;

        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, paymentProduct.getCode(), tppRedirectPreferred);

        //Then:
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getCode()).isEqualTo(FORMAT_ERROR);
    }

    @Test
    public void createPaymentInitiation_Failure_account_does_not_exist() {
        // Given
        SinglePayments payment = getPaymentInitiationRequest(WRONG_IBAN, AMOUNT);
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;

        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, paymentProduct.getCode(), tppRedirectPreferred);

        //Then:
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getCode()).isEqualTo(RESOURCE_UNKNOWN_400);
    }

    @Test
    public void createPaymentInitiation_Failure_payment_not_allowed_to_psu() {
        // Given
        SinglePayments payment = getPaymentInitiationRequest(IBAN, AMOUNT);
        PaymentProduct paymentProduct = PaymentProduct.CBCT;
        boolean tppRedirectPreferred = false;

        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, paymentProduct.getCode(), tppRedirectPreferred);

        //Then:
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getCode()).isEqualTo(PRODUCT_INVALID);
    }

    @Test
    public void createPaymentInitiation_Failure_ASPSP_rejects_due_to_excessive_amount() {
        // Given
        SinglePayments payment = getPaymentInitiationRequest(IBAN, EXCESSIVE_AMOUNT);
        PaymentProduct paymentProduct = PaymentProduct.SCT;
        boolean tppRedirectPreferred = false;

        //When:
        ResponseObject<PaymentInitialisationResponse> actualResponse = paymentService.createPaymentInitiation(payment, paymentProduct.getCode(), tppRedirectPreferred);

        //Then:
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getCode()).isEqualTo(PAYMENT_FAILED);
    }

    //Test additional methods
    private SpiPaymentInitialisationResponse getSpiPaymentResponse(SpiTransactionStatus status) {
        SpiPaymentInitialisationResponse spiPaymentInitialisationResponse = new SpiPaymentInitialisationResponse();
        spiPaymentInitialisationResponse.setTransactionStatus(status);
        spiPaymentInitialisationResponse.setPaymentId(PAYMENT_ID);
        return spiPaymentInitialisationResponse;
    }

    private SinglePayments getPaymentInitiationRequest(String iban, String amountToPay) {
        Amount amount = new Amount();
        amount.setCurrency(CURRENCY);
        amount.setContent(amountToPay);
        BICFI bicfi = new BICFI();
        bicfi.setCode("vnldkvn");
        SinglePayments singlePayments = new SinglePayments();
        singlePayments.setInstructedAmount(amount);
        singlePayments.setDebtorAccount(getReference(iban));
        singlePayments.setCreditorName("Merchant123");
        singlePayments.setPurposeCode(new PurposeCode("BEQNSD"));
        singlePayments.setCreditorAgent(bicfi);
        singlePayments.setCreditorAccount(getReference(iban));
        singlePayments.setPurposeCode(new PurposeCode("BCENECEQ"));
        singlePayments.setRemittanceInformationUnstructured("Ref Number Merchant");
        singlePayments.setRequestedExecutionDate(DATE.plusDays(1));
        singlePayments.setRequestedExecutionTime(TIME.plusHours(1));

        return singlePayments;
    }

    private AccountReference getReference(String iban) {
        AccountReference reference = new AccountReference();
        reference.setIban(iban);
        reference.setCurrency(CURRENCY);

        return reference;
    }

    private PeriodicPayment getPeriodicPayment(String iban, String amountToPay) {
        PeriodicPayment payment = new PeriodicPayment();
        Amount amount = new Amount();
        amount.setCurrency(CURRENCY);
        amount.setContent(amountToPay);
        BICFI bicfi = new BICFI();
        bicfi.setCode("vnldkvn");
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference(iban));
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode(new PurposeCode("BEQNSD"));
        payment.setCreditorAgent(bicfi);
        payment.setCreditorAccount(getReference(iban));
        payment.setPurposeCode(new PurposeCode("BCENECEQ"));
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");

        payment.setStartDate(DATE.plusDays(1));
        payment.setEndDate(DATE.plusMonths(1));
        payment.setDayOfExecution(3);
        payment.setExecutionRule("some rule");
        return payment;
    }

    private AccountDetails getDetails(String iban) {
        return new AccountDetails("123", iban, null, null, null, null, CURRENCY, null, null, null, null, null);
    }*/
}
