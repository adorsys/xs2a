/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.validator.payment.PaymentBusinessValidator;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.TppNotificationDataValidator;
import de.adorsys.psd2.xs2a.service.validator.TppUriHeaderValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.dto.CreatePaymentRequestObject;
import de.adorsys.psd2.xs2a.validator.payment.CountryPaymentValidatorResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePaymentValidatorTest {
    private static final PsuIdData PSU_DATA =
        new PsuIdData("psu id", null, null, null, null);
    private static final PsuIdData EMPTY_PSU_DATA =
        new PsuIdData(null, null, null, null, null);
    private static final MessageError PSU_DATA_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final MessageError BUSINESS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final byte[] PAYMENT_BODY = "some body".getBytes();

    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String INVALID_DOMAIN_MESSAGE = "TPP URIs are not compliant with the domain secured by the eIDAS QWAC certificate of the TPP in the field CN or SubjectAltName of the certificate";

    @Mock
    private PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    @Mock
    private PaymentBusinessValidator paymentBusinessValidator;
    @Mock
    private CountryPaymentValidatorResolver countryPaymentValidatorResolver;
    @Mock
    private TppUriHeaderValidator tppUriHeaderValidator;
    @Mock
    private TppNotificationDataValidator tppNotificationDataValidator;

    @InjectMocks
    private CreatePaymentValidator createPaymentValidator;

    @Test
    void validate_withValidSinglePayment_shouldReturnValid() {
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PSU_DATA, PaymentType.SINGLE);

        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(countryPaymentValidatorResolver.getPaymentBusinessValidator())
            .thenReturn(paymentBusinessValidator);
        when(paymentBusinessValidator.validate(any(), anyString(), any()))
            .thenReturn(de.adorsys.psd2.xs2a.core.service.validator.ValidationResult.valid());

        // When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(buildPayment(), paymentInitiationParameters));

        // Then
        verify(psuDataInInitialRequestValidator).validate(PSU_DATA);
        verify(paymentBusinessValidator).validate(PAYMENT_BODY, PAYMENT_PRODUCT, PaymentType.SINGLE);

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withValidPeriodicPayment_shouldReturnValid() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PSU_DATA, PaymentType.PERIODIC);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(countryPaymentValidatorResolver.getPaymentBusinessValidator())
            .thenReturn(paymentBusinessValidator);
        when(paymentBusinessValidator.validate(any(), anyString(), any()))
            .thenReturn(de.adorsys.psd2.xs2a.core.service.validator.ValidationResult.valid());

        // When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(buildPayment(), paymentInitiationParameters));

        // Then
        verify(psuDataInInitialRequestValidator).validate(PSU_DATA);
        verify(paymentBusinessValidator).validate(PAYMENT_BODY, PAYMENT_PRODUCT, PaymentType.PERIODIC);

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withValidBulkPayment_shouldReturnValid() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PSU_DATA, PaymentType.BULK);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(countryPaymentValidatorResolver.getPaymentBusinessValidator())
            .thenReturn(paymentBusinessValidator);
        when(paymentBusinessValidator.validate(any(), anyString(), any()))
            .thenReturn(de.adorsys.psd2.xs2a.core.service.validator.ValidationResult.valid());

        // When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(buildPayment(), paymentInitiationParameters));

        // Then
        verify(psuDataInInitialRequestValidator).validate(PSU_DATA);
        verify(paymentBusinessValidator).validate(PAYMENT_BODY, PAYMENT_PRODUCT, PaymentType.BULK);

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withValidRawPayment_shouldReturnValid() {
        // Given
        String rawPaymentProduct = "raw";

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PSU_DATA, PaymentType.SINGLE, rawPaymentProduct);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(countryPaymentValidatorResolver.getPaymentBusinessValidator())
            .thenReturn(paymentBusinessValidator);
        when(paymentBusinessValidator.validate(any(), anyString(), any()))
            .thenReturn(de.adorsys.psd2.xs2a.core.service.validator.ValidationResult.valid());

        // When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(buildPayment(), paymentInitiationParameters));

        // Then
        verify(psuDataInInitialRequestValidator).validate(PSU_DATA);
        verify(paymentBusinessValidator).validate(PAYMENT_BODY, rawPaymentProduct, PaymentType.SINGLE);

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidPsuData_shouldReturnErrorFromValidator() {
        //Given
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.invalid(PSU_DATA_VALIDATION_ERROR));

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(EMPTY_PSU_DATA, PaymentType.SINGLE);

        //When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(buildPayment(), paymentInitiationParameters));

        //Then
        verify(psuDataInInitialRequestValidator).validate(EMPTY_PSU_DATA);
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(PSU_DATA_VALIDATION_ERROR);
    }

    @Test
    void validate_withBusinessValidationError_shouldReturnErrorFromValidator() {
        //Given
        when(paymentBusinessValidator.validate(any(), anyString(), any()))
            .thenReturn(de.adorsys.psd2.xs2a.core.service.validator.ValidationResult.invalid(BUSINESS_VALIDATION_ERROR));
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(countryPaymentValidatorResolver.getPaymentBusinessValidator())
            .thenReturn(paymentBusinessValidator);

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(EMPTY_PSU_DATA, PaymentType.SINGLE);

        //When
        ValidationResult validationResult = createPaymentValidator.validate(new CreatePaymentRequestObject(buildPayment(), paymentInitiationParameters));

        //Then
        verify(paymentBusinessValidator).validate(PAYMENT_BODY, PAYMENT_PRODUCT, PaymentType.SINGLE);
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(BUSINESS_VALIDATION_ERROR);
    }

    @Test
    void buildWarningMessages_emptySet() {
        // Given
        Set<TppMessageInformation> emptySet = new HashSet<>();
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(EMPTY_PSU_DATA, PaymentType.SINGLE);
        CreatePaymentRequestObject createPaymentRequestObject = new CreatePaymentRequestObject(buildPayment(), paymentInitiationParameters);

        when(tppUriHeaderValidator.buildWarningMessages(any()))
            .thenReturn(emptySet);
        when(tppNotificationDataValidator.buildWarningMessages(any()))
            .thenReturn(emptySet);

        // When
        Set<TppMessageInformation> actual = createPaymentValidator.buildWarningMessages(createPaymentRequestObject);

        // Then
        assertEquals(actual, emptySet);
        verify(tppUriHeaderValidator, times(1)).buildWarningMessages(any());
        verify(tppNotificationDataValidator, times(1)).buildWarningMessages(any());
    }

    @Test
    void buildWarningMessages_warningsFromUriHeaderValidator() {
        // Given
        Set<TppMessageInformation> emptySet = new HashSet<>();
        Set<TppMessageInformation> uriHeaderValidatorSet = new HashSet<>();
        uriHeaderValidatorSet.add(TppMessageInformation.buildWarning(INVALID_DOMAIN_MESSAGE));
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(EMPTY_PSU_DATA, PaymentType.SINGLE);
        CreatePaymentRequestObject createPaymentRequestObject = new CreatePaymentRequestObject(buildPayment(), paymentInitiationParameters);

        when(tppUriHeaderValidator.buildWarningMessages(any()))
            .thenReturn(uriHeaderValidatorSet);
        when(tppNotificationDataValidator.buildWarningMessages(any()))
            .thenReturn(emptySet);

        // When
        Set<TppMessageInformation> actual = createPaymentValidator.buildWarningMessages(createPaymentRequestObject);

        // Then
        assertEquals(actual, uriHeaderValidatorSet);
        verify(tppUriHeaderValidator, times(1)).buildWarningMessages(any());
        verify(tppNotificationDataValidator, times(1)).buildWarningMessages(any());
    }


    private PaymentInitiationParameters buildPaymentInitiationParameters(PsuIdData psuIdData, PaymentType paymentType) {
        return buildPaymentInitiationParameters(psuIdData, paymentType, PAYMENT_PRODUCT);
    }

    private PaymentInitiationParameters buildPaymentInitiationParameters(PsuIdData psuIdData, PaymentType paymentType, String paymentProduct) {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPsuData(psuIdData);
        requestParameters.setPaymentType(paymentType);
        requestParameters.setPaymentProduct(paymentProduct);
        return requestParameters;
    }

    private byte[] buildPayment() {
        return PAYMENT_BODY;
    }
}
