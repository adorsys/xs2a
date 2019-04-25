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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.SupportedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.dto.CreatePaymentRequestObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreatePaymentValidatorTest {
    private static final PsuIdData PSU_DATA =
        new PsuIdData("psu id", null, null, null);
    private static final PsuIdData EMPTY_PSU_DATA =
        new PsuIdData(null, null, null, null);
    private static final MessageError PSU_DATA_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final MessageError SUPPORTED_ACCOUNT_REFERENCE_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final AccountReference DEBTOR_ACCOUNT =
        new AccountReference(AccountReferenceType.IBAN, "debtor account", Currency.getInstance("EUR"));
    private static final AccountReference CREDITOR_ACCOUNT =
        new AccountReference(AccountReferenceType.IBAN, "debtor account", Currency.getInstance("EUR"));

    @Mock
    private PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    @Mock
    private SupportedAccountReferenceValidator supportedAccountReferenceValidator;
    @Mock
    private StandardPaymentProductsResolver standardPaymentProductsResolver;


    @InjectMocks
    private CreatePaymentValidator createPaymentValidator;

    @Before
    public void setUp() {
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(supportedAccountReferenceValidator.validate(anyCollectionOf(AccountReference.class)))
            .thenReturn(ValidationResult.valid());
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class))).thenReturn(ValidationResult.valid());
    }

    @Test
    public void validate_withValidSinglePayment_shouldReturnValid() {
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PSU_DATA, PaymentType.SINGLE);
        SinglePayment payment = buildSinglePayment(DEBTOR_ACCOUNT, CREDITOR_ACCOUNT);

        // When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(payment, paymentInitiationParameters));

        // Then
        verify(psuDataInInitialRequestValidator).validate(PSU_DATA);
        verify(supportedAccountReferenceValidator).validate(new HashSet<>(Arrays.asList(DEBTOR_ACCOUNT, CREDITOR_ACCOUNT)));

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withValidPeriodicPayment_shouldReturnValid() {
        // Given

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PSU_DATA, PaymentType.PERIODIC);
        PeriodicPayment payment = buildPeriodicPayment(DEBTOR_ACCOUNT, CREDITOR_ACCOUNT);

        // When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(payment, paymentInitiationParameters));

        // Then
        verify(psuDataInInitialRequestValidator).validate(PSU_DATA);
        verify(supportedAccountReferenceValidator).validate(new HashSet<>(Arrays.asList(DEBTOR_ACCOUNT, CREDITOR_ACCOUNT)));

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withValidBulkPayment_shouldReturnValid() {
        // Given

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PSU_DATA, PaymentType.BULK);
        BulkPayment payment = buildBulkPayment(DEBTOR_ACCOUNT, CREDITOR_ACCOUNT);

        // When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(payment, paymentInitiationParameters));

        // Then
        verify(psuDataInInitialRequestValidator).validate(PSU_DATA);
        verify(supportedAccountReferenceValidator).validate(new HashSet<>(Arrays.asList(DEBTOR_ACCOUNT, CREDITOR_ACCOUNT)));

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withValidRawPayment_shouldReturnValid_emptyReferences() {
        // Given
        String rawPaymentProduct = "raw";

        when(standardPaymentProductsResolver.isRawPaymentProduct(rawPaymentProduct)).thenReturn(true);

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PSU_DATA, PaymentType.SINGLE, rawPaymentProduct);
        SinglePayment payment = buildSinglePayment(DEBTOR_ACCOUNT, CREDITOR_ACCOUNT);

        // When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(payment, paymentInitiationParameters));

        // Then
        verify(psuDataInInitialRequestValidator).validate(PSU_DATA);
        verify(supportedAccountReferenceValidator).validate(Collections.emptySet());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withInvalidPsuData_shouldReturnErrorFromValidator() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.invalid(PSU_DATA_VALIDATION_ERROR));

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(EMPTY_PSU_DATA, PaymentType.SINGLE);
        SinglePayment payment = buildSinglePayment();

        //When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(payment, paymentInitiationParameters));

        //Then
        verify(psuDataInInitialRequestValidator).validate(EMPTY_PSU_DATA);
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(PSU_DATA_VALIDATION_ERROR);
    }

    @Test
    public void validate_withUnsupportedAccountReference_shouldReturnErrorFromValidator() {
        //Given
        when(supportedAccountReferenceValidator.validate(anyCollectionOf(AccountReference.class)))
            .thenReturn(ValidationResult.invalid(SUPPORTED_ACCOUNT_REFERENCE_VALIDATION_ERROR));

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(EMPTY_PSU_DATA, PaymentType.SINGLE);
        AccountReference debtorAccount = new AccountReference(AccountReferenceType.IBAN, "debtor iban", Currency.getInstance("EUR"));
        AccountReference creditorAccount = new AccountReference(AccountReferenceType.IBAN, "creditor iban", Currency.getInstance("EUR"));
        SinglePayment payment = buildSinglePayment(debtorAccount, creditorAccount);

        //When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(payment, paymentInitiationParameters));

        //Then
        verify(supportedAccountReferenceValidator).validate(new HashSet<>(Arrays.asList(debtorAccount, creditorAccount)));
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(SUPPORTED_ACCOUNT_REFERENCE_VALIDATION_ERROR);
    }

    private PaymentInitiationParameters buildPaymentInitiationParameters(PsuIdData psuIdData, PaymentType paymentType) {
        return buildPaymentInitiationParameters(psuIdData, paymentType, null);
    }

    private PaymentInitiationParameters buildPaymentInitiationParameters(PsuIdData psuIdData, PaymentType paymentType, String paymentProduct) {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPsuData(psuIdData);
        requestParameters.setPaymentType(paymentType);
        requestParameters.setPaymentProduct(paymentProduct);
        return requestParameters;
    }

    private SinglePayment buildSinglePayment() {
        return buildSinglePayment(null, null);
    }

    private SinglePayment buildSinglePayment(AccountReference debtorAccount, AccountReference creditorAccount) {
        SinglePayment singlePayment = new SinglePayment();
        singlePayment.setDebtorAccount(debtorAccount);
        singlePayment.setCreditorAccount(creditorAccount);
        return singlePayment;
    }

    private PeriodicPayment buildPeriodicPayment(AccountReference debtorAccount, AccountReference creditorAccount) {
        PeriodicPayment periodicPayment = new PeriodicPayment();
        periodicPayment.setDebtorAccount(debtorAccount);
        periodicPayment.setCreditorAccount(creditorAccount);
        return periodicPayment;
    }

    private BulkPayment buildBulkPayment(AccountReference debtorAccount, AccountReference creditorAccount) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setDebtorAccount(debtorAccount);
        bulkPayment.setPayments(Collections.singletonList(buildSinglePayment(debtorAccount, creditorAccount)));
        return bulkPayment;
    }
}
