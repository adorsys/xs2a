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

package de.adorsys.psd2.xs2a.web.validator.body.payment;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.validator.payment.PaymentBodyFieldsValidator;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.validator.payment.CountryPaymentValidatorResolver;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.CurrencyValidator;
import de.adorsys.psd2.xs2a.web.validator.body.DateFieldValidator;
import de.adorsys.psd2.xs2a.web.validator.body.FieldLengthValidator;
import de.adorsys.psd2.xs2a.web.validator.body.TppRedirectUriBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateFields;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.web.validator.body.payment.PaymentBodyValidatorImpl.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentBodyValidatorImplTest {
    private static final String JSON_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAIN_PAYMENT_PRODUCT = "pain.001-sepa-credit-transfers";
    private static final String PAYMENT_SERVICE = "some payment service";
    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";
    private static final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";
    private static final String WRONG_FREQUENCY_STRING = "wrong frequency";
    private static final String WRONG_BATCH_BOOKING_PREFERRED_STRING = "not boolean string";
    private static final String PURPOSE_CODE = "BKDF";

    private static final MessageError DAY_OF_EXECUTION_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_INVALID_DAY_OF_EXECUTION));
    private static final MessageError REQUESTED_EXECUTION_DATE_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, "requestedExecutionDate", "ISO_DATE", "YYYY-MM-DD"));
    private static final MessageError REQUESTED_EXECUTION_TIME_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, "requestedExecutionTime", "ISO_DATE_TIME", "YYYY-MM-DD'T'HH:mm:ssZ"));

    private static final MessageError PURPOSE_CODE_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, PURPOSE_CODE_FIELD_NAME));
    private static final MessageError NO_FREQUENCY =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, FREQUENCY_FIELD_NAME));
    private static final MessageError WRONG_FREQUENCY =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, FREQUENCY_FIELD_NAME));
    private static final MessageError WRONG_BATCH_BOOKING_PREFERRED =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(FORMAT_ERROR_BOOLEAN_VALUE, BATCH_BOOKING_PREFERRED_FIELD_NAME));
    private static final MessageError WRONG_CURRENCY_FORMAT =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, CURRENCY_STRING));
    private static final String VALID_FREQUENCY_CODE = "Annual";
    private static final String CURRENCY_FIELD_NAME = "currency";
    private static final String CURRENCY_VALUE = "EUR";

    private PaymentBodyValidatorImpl validator;
    private MessageError messageError;

    @Mock
    private StandardPaymentProductsResolver standardPaymentProductsResolver;
    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;
    @Mock
    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;
    private MockHttpServletRequest mockRequest;
    @Mock
    private FieldExtractor fieldExtractor;
    @Mock
    private CountryPaymentValidatorResolver countryPaymentValidatorResolver;
    @Mock
    private PaymentBodyFieldsValidator paymentBodyFieldsValidator;
    @Mock
    private DateFieldValidator dateFieldValidator;
    @Mock
    private CurrencyValidator currencyValidator;
    @Mock
    private PathParameterExtractor pathParameterExtractor;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        messageError = new MessageError(ErrorType.PIS_400);
        ErrorBuildingService errorService = new ErrorBuildingServiceMock(ErrorType.PIS_400);
        validator = new PaymentBodyValidatorImpl(errorService, xs2aObjectMapper, standardPaymentProductsResolver, tppRedirectUriBodyValidator,
                                                 dateFieldValidator, fieldExtractor, currencyValidator, pathParameterExtractor, countryPaymentValidatorResolver,
                                                 new FieldLengthValidator(errorService));
    }

    @Test
    void validate_shouldExecuteSpecificValidator() {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(countryPaymentValidatorResolver.getPaymentBodyFieldValidator()).thenReturn(paymentBodyFieldsValidator);
        when(paymentBodyFieldsValidator.validate(mockRequest, PAYMENT_SERVICE, messageError)).thenReturn(messageError);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        verify(tppRedirectUriBodyValidator, times(1)).validate(mockRequest, messageError);
        verify(countryPaymentValidatorResolver, times(1)).getPaymentBodyFieldValidator();
        verify(paymentBodyFieldsValidator, times(1)).validate(mockRequest, PAYMENT_SERVICE, messageError);
    }

    @Test
    void validate_dayOfExecutionWrongValue_wrongFormat_error() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(fieldExtractor.extractField(mockRequest, PURPOSE_CODE_FIELD_NAME, messageError))
            .thenReturn(Optional.of(PURPOSE_CODE));

        doAnswer((Answer<Void>) invocation -> {
            messageError.addTppMessage(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_INVALID_DAY_OF_EXECUTION));
            return null;
        }).when(dateFieldValidator).validateDayOfExecution(mockRequest, messageError);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(DAY_OF_EXECUTION_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    void validate_requestedExecutionDateWrongValue_wrongFormat_error() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(dateFieldValidator.validateDateFormat(mockRequest, Xs2aRequestBodyDateFields.PAYMENT_DATE_FIELDS.getDateFields(), messageError))
            .then((Answer<Void>) invocation -> {
                TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, "requestedExecutionDate", "ISO_DATE", "YYYY-MM-DD");
                messageError.addTppMessage(tppMessageInformation);
                return null;
            });

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(REQUESTED_EXECUTION_DATE_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    void validate_requestedExecutionDateCorrectValue_success() {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(countryPaymentValidatorResolver.getPaymentBodyFieldValidator()).thenReturn(paymentBodyFieldsValidator);
        when(paymentBodyFieldsValidator.validate(mockRequest, PAYMENT_SERVICE, messageError)).thenReturn(messageError);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        verify(tppRedirectUriBodyValidator, times(1)).validate(mockRequest, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_requestedExecutionTimeWrongValue_wrongFormat_error() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(dateFieldValidator.validateDateFormat(mockRequest, Xs2aRequestBodyDateFields.PAYMENT_DATE_FIELDS.getDateFields(), messageError))
            .then((Answer<Void>) invocation -> {
                TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, "requestedExecutionTime", "ISO_DATE_TIME", "YYYY-MM-DD'T'HH:mm:ssZ");
                messageError.addTppMessage(tppMessageInformation);
                return null;
            });

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(REQUESTED_EXECUTION_TIME_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    void validate_requestedExecutionTimeCorrectValue_success() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(countryPaymentValidatorResolver.getPaymentBodyFieldValidator()).thenReturn(paymentBodyFieldsValidator);
        when(paymentBodyFieldsValidator.validate(mockRequest, PAYMENT_SERVICE, messageError)).thenReturn(messageError);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_Currency_success() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(countryPaymentValidatorResolver.getPaymentBodyFieldValidator()).thenReturn(paymentBodyFieldsValidator);
        when(paymentBodyFieldsValidator.validate(mockRequest, PAYMENT_SERVICE, messageError)).thenReturn(messageError);

        when(fieldExtractor.extractOptionalList(mockRequest, CURRENCY_FIELD_NAME)).thenReturn(Collections.singletonList(CURRENCY_VALUE));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_Currency_WrongFormat() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        String invalidCurrency = "EURf";
        when(fieldExtractor.extractOptionalList(mockRequest, CURRENCY_STRING)).thenReturn(Collections.singletonList(invalidCurrency));

        doAnswer((Answer<Void>) invocation -> {
            TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, "currency");
            messageError.addTppMessage(tppMessageInformation);
            return null;
        }).when(currencyValidator).validateCurrency(invalidCurrency, messageError);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(WRONG_CURRENCY_FORMAT, messageError);
        assertArrayEquals(new Object[]{CURRENCY_STRING}, messageError.getTppMessage().getTextParameters());
    }


    @Test
    void validate_rawPaymentProduct_shouldNotBeValidated() throws IOException {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(PAIN_PAYMENT_PRODUCT))
            .thenReturn(true);

        Map<String, String> templates = buildTemplateVariables(PAIN_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
        // noinspection unchecked
        verify(xs2aObjectMapper, never()).readValue(any(InputStream.class), any(Class.class));
        verify(tppRedirectUriBodyValidator, never()).validate(mockRequest, messageError);
    }

    @Test
    void validate_purposeCodes_shouldReturnError() {
        // Given
        String purposeCode = "CDCQ";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(fieldExtractor.extractField(mockRequest, PURPOSE_CODE_FIELD_NAME, messageError))
            .thenReturn(Optional.of(purposeCode));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(PURPOSE_CODE_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    void validate_NullFrequency_shouldReturnError() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PERIODIC_PAYMENT_PATH_VAR);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(NO_FREQUENCY, messageError);
    }

    @Test
    void validate_WrongFormatFrequency_shouldReturnError() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PERIODIC_PAYMENT_PATH_VAR);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(fieldExtractor.extractField(mockRequest, FREQUENCY_FIELD_NAME, messageError)).thenReturn(Optional.of(WRONG_FREQUENCY_STRING));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(WRONG_FREQUENCY, messageError);
    }

    @Test
    void validate_WrongBatchBookingPreferredForBulk_shouldReturnError() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, BULK_PAYMENT_PATH_VAR);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(fieldExtractor.extractOptionalField(mockRequest, BATCH_BOOKING_PREFERRED_FIELD_NAME)).thenReturn(Optional.of(WRONG_BATCH_BOOKING_PREFERRED_STRING));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(WRONG_BATCH_BOOKING_PREFERRED, messageError);
    }

    private Map<String, String> buildTemplateVariables(String paymentProduct, String paymentService) {
        Map<String, String> templates = new HashMap<>();
        templates.put(PAYMENT_PRODUCT_PATH_VAR, paymentProduct);
        templates.put(PAYMENT_SERVICE_PATH_VAR, paymentService);
        return templates;
    }
}
