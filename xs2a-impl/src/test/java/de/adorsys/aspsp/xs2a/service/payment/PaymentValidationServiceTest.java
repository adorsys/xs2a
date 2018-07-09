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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.AccountService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentValidationServiceTest {
    private static final String AMOUNT = "100";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final LocalDate DATE = LocalDate.from(LocalDate.now());
    private static final LocalDateTime TIME = LocalDate.now().atTime(1, 0);
    private static final LocalDate WRONG_DATE = LocalDate.from(LocalDate.now().minusDays(1));
    private static final LocalDateTime WRONG_TIME = LocalDate.now().atStartOfDay();
    private static final String IBAN = "DE123456789";
    private static final String WRONG_IBAN = "wrong_iban";
    private static final String ALLOWED_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String FORBIDDEN_PAYMENT_PRODUCT = "cross-border-credit-transfers";

    @InjectMocks
    PaymentValidationService validationService;
    @Mock
    AccountService accountService;

    @Before
    public void setUp() {
        when(accountService.getAccountDetailsByAccountReference(getReference(IBAN))).thenReturn(Optional.of(getDetails(IBAN)));
        when(accountService.getAccountDetailsByAccountReference(getReference(WRONG_IBAN))).thenReturn(Optional.empty());

        when(accountService.isInvalidPaymentProductForPsu(getReference(IBAN), ALLOWED_PAYMENT_PRODUCT)).thenReturn(false);
        when(accountService.isInvalidPaymentProductForPsu(getReference(IBAN), FORBIDDEN_PAYMENT_PRODUCT)).thenReturn(true);

    }

    @Test
    public void validatePeriodicPayment() {
        //Given
        PeriodicPayment payment = getPeriodicPayment(IBAN, AMOUNT, DATE, TIME, DATE, DATE.plusDays(1));
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;
        //When
        Optional<MessageErrorCode> actualResponse = validationService.validatePeriodicPayment(payment, paymentProduct);
        //Then
        assertThat(actualResponse.isPresent()).isEqualTo(false);
    }

    @Test
    public void validatePeriodicPayment_Fail_null_payment() {
        //Given
        PeriodicPayment payment = null;
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;

        periodicPaymentTest(payment, paymentProduct, FORMAT_ERROR);
    }

    @Test
    public void validatePeriodicPayment_Fail_wrong_execDate() {
        //Given
        PeriodicPayment payment = getPeriodicPayment(IBAN, AMOUNT, WRONG_DATE, TIME, DATE, DATE.plusDays(1));
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;

        periodicPaymentTest(payment, paymentProduct, EXECUTION_DATE_INVALID);
    }

    @Test
    public void validatePeriodicPayment_Fail_wrong_execTime() {
        //Given
        PeriodicPayment payment = getPeriodicPayment(IBAN, AMOUNT, DATE, WRONG_TIME, DATE, DATE.plusDays(1));
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;

        periodicPaymentTest(payment, paymentProduct, EXECUTION_DATE_INVALID);
    }

    @Test
    public void validatePeriodicPayment_Fail_wrong_startDate() {
        //Given
        PeriodicPayment payment = getPeriodicPayment(IBAN, AMOUNT, DATE, TIME, WRONG_DATE, DATE.plusDays(1));
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;

        periodicPaymentTest(payment, paymentProduct, EXECUTION_DATE_INVALID);
    }

    @Test
    public void validatePeriodicPayment_Fail_wrong_endDate_same_as_start() {
        //Given
        PeriodicPayment payment = getPeriodicPayment(IBAN, AMOUNT, DATE, TIME, DATE, DATE);
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;

        periodicPaymentTest(payment, paymentProduct, EXECUTION_DATE_INVALID);
    }

    @Test
    public void validatePeriodicPayment_Fail_wrong_iban() {
        //Given
        PeriodicPayment payment = getPeriodicPayment(WRONG_IBAN, AMOUNT, DATE, TIME, DATE, DATE.plusDays(1));
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;

        periodicPaymentTest(payment, paymentProduct, RESOURCE_UNKNOWN_400);
    }

    private void periodicPaymentTest(PeriodicPayment payment, String paymentProduct, MessageErrorCode errorCode) {
        //When
        Optional<MessageErrorCode> actualResponse = validationService.validatePeriodicPayment(payment, paymentProduct);
        //Then
        assertTrue(actualResponse.isPresent());
        assertThat(actualResponse.get(), Matchers.is(equalTo(errorCode)));
    }

    @Test
    public void validateSinglePayment() {
        //Given
        SinglePayments payment = getPayment(IBAN, AMOUNT, DATE, TIME);
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;
        //When
        Optional<MessageErrorCode> actualResponse = validationService.validateSinglePayment(payment, paymentProduct);
        //Then
        assertThat(actualResponse.isPresent()).isEqualTo(false);
    }

    @Test
    public void validateSinglePayment_Fail_null_payment() {
        //Given
        SinglePayments payment = null;
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;

        singlePaymentTest(payment, paymentProduct, FORMAT_ERROR);
    }

    @Test
    public void validateSinglePayment_Fail_wrong_execDate() {
        //Given
        SinglePayments payment = getPayment(IBAN, AMOUNT, WRONG_DATE, TIME);
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;

        singlePaymentTest(payment, paymentProduct, EXECUTION_DATE_INVALID);
    }

    @Test
    public void validateSinglePayment_Fail_wrong_execTime() {
        //Given
        SinglePayments payment = getPayment(IBAN, AMOUNT, DATE, WRONG_TIME);
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;

        singlePaymentTest(payment, paymentProduct, EXECUTION_DATE_INVALID);
    }

    @Test
    public void validateSinglePayment_Fail_wrong_iban() {
        //Given
        SinglePayments payment = getPayment(WRONG_IBAN, AMOUNT, DATE, TIME);
        String paymentProduct = ALLOWED_PAYMENT_PRODUCT;

        singlePaymentTest(payment, paymentProduct, RESOURCE_UNKNOWN_400);
    }

    private void singlePaymentTest(SinglePayments payment, String paymentProduct, MessageErrorCode errorCode) {
        //When
        Optional<MessageErrorCode> actualResponse = validationService.validateSinglePayment(payment, paymentProduct);
        //Then
        assertTrue(actualResponse.isPresent());
        assertThat(actualResponse.get(), Matchers.is(equalTo(errorCode)));
    }

    private PeriodicPayment getPeriodicPayment(String iban, String amountToPay, LocalDate execution, LocalDateTime executionTime, LocalDate start, LocalDate end) {
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
        payment.setRequestedExecutionDate(execution);
        payment.setRequestedExecutionTime(executionTime);

        payment.setStartDate(start);
        payment.setEndDate(end);
        payment.setDayOfExecution(3);
        payment.setExecutionRule("some rule");
        return payment;
    }

    private SinglePayments getPayment(String iban, String amountToPay, LocalDate execution, LocalDateTime executionTime) {
        SinglePayments payment = new SinglePayments();
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
        payment.setRequestedExecutionDate(execution);
        payment.setRequestedExecutionTime(executionTime);

        return payment;
    }

    private AccountDetails getDetails(String iban) {
        return new AccountDetails("123", iban, null, null, null, null, CURRENCY, null, null, null, null, null);
    }

    private AccountReference getReference(String iban) {
        AccountReference reference = new AccountReference();
        reference.setIban(iban);
        reference.setCurrency(CURRENCY);

        return reference;
    }
}
