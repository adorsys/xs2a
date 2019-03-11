/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.AccountReferenceValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PERIOD_INVALID;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentValidationServiceTest {

    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final String PAYMENT_ID = "12345";
    private static final String IBAN = "DE15500105172295759744";
    private static final String WRONG_IBAN = "ZZ33300105172295759744";
    private static final String AMOUNT = "100";
    private static final OffsetDateTime START_DATE_CORRECT = LocalDate.of(2050, 12, 12).atTime(OffsetTime.MIN);
    private static final OffsetDateTime END_DATE_CORRECT = LocalDate.of(2060, 12, 12).atTime(OffsetTime.MIN);
    private static final OffsetDateTime START_DATE_WRONG = LocalDate.of(2010, 12, 12).atTime(OffsetTime.MIN);
    private static final OffsetDateTime END_DATE_WRONG = LocalDate.of(2005, 12, 12).atTime(OffsetTime.MIN);
    private static final OffsetDateTime REQUESTED_EXECUTION_DATE_CORRECT = LocalDate.of(2050, 12, 12).atTime(OffsetTime.MIN);
    private static final OffsetDateTime REQUESTED_EXECUTION_DATE_WRONG = LocalDate.of(2005, 12, 12).atTime(OffsetTime.MIN);
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final List<PsuIdData> PSU_DATA_LIST = new ArrayList<>();

    @InjectMocks
    private PaymentValidationService paymentValidationService;
    @Mock
    private AccountReferenceValidationService referenceValidationService;

    @Test
    public void validateSinglePayment_Success() {
        // Given
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildValidResponse());

        // When
        ResponseObject actualResponse = paymentValidationService.validateSinglePayment(buildSinglePayment(IBAN, AMOUNT, REQUESTED_EXECUTION_DATE_CORRECT));

        // Then
        assertThat(actualResponse.hasError()).isFalse();
    }

    @Test
    public void validatePeriodicPayment_Success() {
        // Given
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildValidResponse());

        // When
        ResponseObject actualResponse = paymentValidationService.validatePeriodicPayment(buildPeriodicPayment(IBAN, AMOUNT, START_DATE_CORRECT, END_DATE_CORRECT));

        // Then
        assertThat(actualResponse.hasError()).isFalse();
    }

    @Test
    public void validateBulkPayment_Success() {
        // Given
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildValidResponse());

        // When
        ResponseObject actualResponse = paymentValidationService.validateBulkPayment(buildBulkPayment(IBAN, AMOUNT, REQUESTED_EXECUTION_DATE_CORRECT));

        // Then
        assertThat(actualResponse.hasError()).isFalse();
    }

    @Test
    public void validateSinglePaymentWrongIban_Error() {
        // Given
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildFormatErrorResponse());
        // When
        ResponseObject<SinglePaymentInitiationResponse> actualResponse = paymentValidationService.validateSinglePayment(buildSinglePayment(WRONG_IBAN, AMOUNT, REQUESTED_EXECUTION_DATE_CORRECT));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(PIS_400);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(FORMAT_ERROR);
    }

    @Test
    public void validateSinglePaymentWrongDate_Error() {
        // Given
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildValidResponse());


        // When
        ResponseObject actualResponse = paymentValidationService.validateSinglePayment(buildSinglePayment(IBAN, AMOUNT, REQUESTED_EXECUTION_DATE_WRONG));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(PIS_400);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(PERIOD_INVALID);
    }

    @Test
    public void validatePeriodicPaymentWrongIban_Error() {
        // Given
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildFormatErrorResponse());

        // When
        ResponseObject<SinglePaymentInitiationResponse> actualResponse = paymentValidationService.validatePeriodicPayment(buildPeriodicPayment(WRONG_IBAN, AMOUNT, START_DATE_CORRECT, END_DATE_CORRECT));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(PIS_400);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(FORMAT_ERROR);
    }

    @Test
    public void validatePeriodicPaymentWrongStartDate_Error() {
        // Given
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildValidResponse());
        // When
        ResponseObject<SinglePaymentInitiationResponse> actualResponse = paymentValidationService.validatePeriodicPayment(buildPeriodicPayment(IBAN, AMOUNT, START_DATE_WRONG, END_DATE_CORRECT));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(PIS_400);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(PERIOD_INVALID);
    }

    @Test
    public void validatePeriodicPaymentWrongEndDate_Error() {
        // Given
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildValidResponse());
        // When
        ResponseObject<SinglePaymentInitiationResponse> actualResponse = paymentValidationService.validatePeriodicPayment(buildPeriodicPayment(IBAN, AMOUNT, START_DATE_CORRECT, END_DATE_WRONG));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(PIS_400);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(PERIOD_INVALID);
    }

    @Test
    public void validateBulkPaymentWrongIban_Error() {
        // Given
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildFormatErrorResponse());
        // When
        ResponseObject<SinglePaymentInitiationResponse> actualResponse = paymentValidationService.validateBulkPayment(buildBulkPayment(WRONG_IBAN, AMOUNT, REQUESTED_EXECUTION_DATE_CORRECT));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(PIS_400);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(FORMAT_ERROR);
    }

    @Test
    public void validateBulkPaymentWrongDate_Error() {
        // Given
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildValidResponse());

        // When
        ResponseObject actualResponse = paymentValidationService.validateBulkPayment(buildBulkPayment(IBAN, AMOUNT, REQUESTED_EXECUTION_DATE_WRONG));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(PIS_400);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(PERIOD_INVALID);
    }

    private BulkPayment buildBulkPayment(String iban, String amountToPay, OffsetDateTime requestedExecutionDate) {
        BulkPayment payment = new BulkPayment();
        payment.setPaymentId(PAYMENT_ID);
        payment.setPayments(Collections.singletonList(buildSinglePayment(iban, amountToPay, requestedExecutionDate)));
        payment.setPsuDataList(PSU_DATA_LIST);
        payment.setBatchBookingPreferred(true);
        payment.setDebtorAccount(buildAccountReference(iban));
        payment.setTransactionStatus(TRANSACTION_STATUS);
        payment.setRequestedExecutionDate(requestedExecutionDate.toLocalDate());
        return payment;
    }

    private SinglePayment buildSinglePayment(String iban, String amountToPay, OffsetDateTime requestedExecutionDate) {
        SinglePayment singlePayments = new SinglePayment();
        singlePayments.setEndToEndIdentification(PAYMENT_ID);
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(CURRENCY);
        amount.setAmount(amountToPay);
        singlePayments.setInstructedAmount(amount);
        singlePayments.setDebtorAccount(buildAccountReference(iban));
        singlePayments.setCreditorAccount(buildAccountReference(iban));
        singlePayments.setRequestedExecutionDate(requestedExecutionDate.toLocalDate());
        singlePayments.setRequestedExecutionTime(requestedExecutionDate);
        return singlePayments;
    }

    private PeriodicPayment buildPeriodicPayment(String iban, String amountToPay, OffsetDateTime beginDate, OffsetDateTime endDate) {
        PeriodicPayment periodicPayment = new PeriodicPayment();
        periodicPayment.setEndToEndIdentification(PAYMENT_ID);
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(CURRENCY);
        amount.setAmount(amountToPay);
        periodicPayment.setStartDate(beginDate.toLocalDate());
        periodicPayment.setEndDate(endDate.toLocalDate());
        periodicPayment.setInstructedAmount(amount);
        periodicPayment.setDebtorAccount(buildAccountReference(iban));
        periodicPayment.setCreditorAccount(buildAccountReference(iban));
        periodicPayment.setRequestedExecutionDate(LocalDate.now());
        periodicPayment.setRequestedExecutionTime(OffsetDateTime.now());
        return periodicPayment;
    }

    private AccountReference buildAccountReference(String iban) {
        AccountReference reference = new AccountReference();
        reference.setIban(iban);
        reference.setCurrency(CURRENCY);

        return reference;
    }

    private ResponseObject buildValidResponse() {
        return ResponseObject.builder().build();
    }

    private ResponseObject buildFormatErrorResponse() {
        return ResponseObject.builder()
                   .fail(PIS_400, of(FORMAT_ERROR))
                   .build();
    }
}
