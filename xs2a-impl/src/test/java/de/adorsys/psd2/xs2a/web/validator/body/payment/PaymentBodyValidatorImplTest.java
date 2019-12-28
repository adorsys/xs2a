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
import de.adorsys.psd2.xs2a.web.converter.LocalDateConverter;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.CurrencyValidator;
import de.adorsys.psd2.xs2a.web.validator.body.DateFieldValidator;
import de.adorsys.psd2.xs2a.web.validator.body.TppRedirectUriBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.web.validator.body.payment.PaymentBodyValidatorImpl.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentBodyValidatorImplTest {

    private static final String JSON_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAIN_PAYMENT_PRODUCT = "pain.001-sepa-credit-transfers";
    private static final String PAYMENT_SERVICE = "some payment service";
    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";
    private static final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";
    private static final String DAY_OF_EXECUTION_FIELD_NAME = "dayOfExecution";
    private static final String REQUESTED_EXECUTION_DATE_FIELD_NAME = "requestedExecutionDate";
    private static final String REQUESTED_EXECUTION_TIME_FIELD_NAME = "requestedExecutionTime";
    private static final String CORRECT_DAY_OF_MONTH = "6";
    private static final String WRONG_DAY_OF_MONTH = "666";
    private static final String CORRECT_FORMAT_DATE = "2021-10-10";
    private static final String CORRECT_FORMAT_TIME = "2019-01-01T12:00:00+01:00";
    private static final String WRONG_FORMAT_DATE = "07/01/2019";
    private static final String WRONG_FORMAT_TIME = "07/01/2019 00:00:00";
    private static final String WRONG_FREQUENCY_STRING = "wrong frequency";
    private static final String WRONG_BATCH_BOOKING_PREFERRED_STRING = "not boolean string";

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

    @Before
    public void setUp() {
        mockRequest = new MockHttpServletRequest();
        messageError = new MessageError(ErrorType.PIS_400);
        ErrorBuildingService errorService = new ErrorBuildingServiceMock(ErrorType.PIS_400);
        PathParameterExtractor pathParameterExtractor = new PathParameterExtractor();
        CurrencyValidator currencyValidator = new CurrencyValidator(errorService);
        DateFieldValidator dateFieldValidator = new DateFieldValidator(errorService, new LocalDateConverter(), fieldExtractor);
        validator = new PaymentBodyValidatorImpl(errorService, xs2aObjectMapper,
                                                 standardPaymentProductsResolver, tppRedirectUriBodyValidator,
                                                 dateFieldValidator, fieldExtractor, currencyValidator, pathParameterExtractor,
                                                 countryPaymentValidatorResolver);
        when(standardPaymentProductsResolver.isRawPaymentProduct(eq(PAIN_PAYMENT_PRODUCT)))
            .thenReturn(true);
        when(standardPaymentProductsResolver.isRawPaymentProduct(eq(JSON_PAYMENT_PRODUCT)))
            .thenReturn(false);
    }

    @Test
    public void validate_shouldExecuteSpecificValidator() {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);

        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

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
    public void validate_dayOfExecutionWrongValue_wrongFormat_error() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(fieldExtractor.extractField(mockRequest, DAY_OF_EXECUTION_FIELD_NAME, messageError))
            .thenReturn(Optional.of(WRONG_DAY_OF_MONTH));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(DAY_OF_EXECUTION_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    public void validate_requestedExecutionDateWrongValue_wrongFormat_error() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(fieldExtractor.extractField(mockRequest, DAY_OF_EXECUTION_FIELD_NAME, messageError)).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));
        when(fieldExtractor.extractField(mockRequest, REQUESTED_EXECUTION_DATE_FIELD_NAME, messageError)).thenReturn(Optional.of(WRONG_FORMAT_DATE));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(REQUESTED_EXECUTION_DATE_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    public void validate_requestedExecutionDateCorrectValue_success() {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(fieldExtractor.extractField(mockRequest, DAY_OF_EXECUTION_FIELD_NAME, messageError)).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));
        when(fieldExtractor.extractField(mockRequest, REQUESTED_EXECUTION_DATE_FIELD_NAME, messageError)).thenReturn(Optional.of(CORRECT_FORMAT_DATE));

        when(countryPaymentValidatorResolver.getPaymentBodyFieldValidator()).thenReturn(paymentBodyFieldsValidator);
        when(paymentBodyFieldsValidator.validate(mockRequest, PAYMENT_SERVICE, messageError)).thenReturn(messageError);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        verify(tppRedirectUriBodyValidator, times(1)).validate(mockRequest, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_requestedExecutionTimeWrongValue_wrongFormat_error() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(fieldExtractor.extractField(mockRequest, DAY_OF_EXECUTION_FIELD_NAME, messageError)).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));
        when(fieldExtractor.extractField(mockRequest, REQUESTED_EXECUTION_TIME_FIELD_NAME, messageError)).thenReturn(Optional.of(WRONG_FORMAT_TIME));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(REQUESTED_EXECUTION_TIME_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    public void validate_requestedExecutionTimeCorrectValue_success() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(fieldExtractor.extractField(mockRequest, DAY_OF_EXECUTION_FIELD_NAME, messageError)).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));
        when(fieldExtractor.extractField(mockRequest, REQUESTED_EXECUTION_TIME_FIELD_NAME, messageError)).thenReturn(Optional.of(CORRECT_FORMAT_TIME));

        when(countryPaymentValidatorResolver.getPaymentBodyFieldValidator()).thenReturn(paymentBodyFieldsValidator);
        when(paymentBodyFieldsValidator.validate(mockRequest, PAYMENT_SERVICE, messageError)).thenReturn(messageError);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_Currency_success() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(fieldExtractor.extractField(mockRequest, DAY_OF_EXECUTION_FIELD_NAME, messageError)).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));
        when(fieldExtractor.extractField(mockRequest, REQUESTED_EXECUTION_TIME_FIELD_NAME, messageError)).thenReturn(Optional.of(CORRECT_FORMAT_TIME));
        when(fieldExtractor.extractOptionalList(mockRequest, CURRENCY_STRING)).thenReturn(Collections.singletonList("EUR"));

        when(countryPaymentValidatorResolver.getPaymentBodyFieldValidator()).thenReturn(paymentBodyFieldsValidator);
        when(paymentBodyFieldsValidator.validate(mockRequest, PAYMENT_SERVICE, messageError)).thenReturn(messageError);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_Currency_WrongFormat() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(fieldExtractor.extractField(mockRequest, DAY_OF_EXECUTION_FIELD_NAME, messageError)).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));
        when(fieldExtractor.extractField(mockRequest, REQUESTED_EXECUTION_TIME_FIELD_NAME, messageError)).thenReturn(Optional.of(CORRECT_FORMAT_TIME));
        when(fieldExtractor.extractOptionalList(mockRequest, CURRENCY_STRING)).thenReturn(Collections.singletonList("EURf"));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(WRONG_CURRENCY_FORMAT, messageError);
        assertArrayEquals(new Object[]{CURRENCY_STRING}, messageError.getTppMessage().getTextParameters());
    }


    @Test
    public void validate_rawPaymentProduct_shouldNotBeValidated() throws IOException {
        // Given
        Map<String, String> templates = buildTemplateVariables(PAIN_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
        // noinspection unchecked
        verify(xs2aObjectMapper, never()).readValue(any(InputStream.class), any(Class.class));
        verify(tppRedirectUriBodyValidator, never()).validate(mockRequest, messageError);
    }

    @Test
    public void validate_purposeCodes_shouldReturnError() {
        // Given
        String purposeCode = "CDCQ";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(fieldExtractor.extractList(mockRequest, PURPOSE_CODE_FIELD_NAME, messageError))
            .thenReturn(Collections.singletonList(purposeCode));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(PURPOSE_CODE_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    public void validate_NullFrequency_shouldReturnError() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PERIODIC_PAYMENT_PATH_VAR);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(NO_FREQUENCY, messageError);
    }

    @Test
    public void validate_WrongFormatFrequency_shouldReturnError() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PERIODIC_PAYMENT_PATH_VAR);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(fieldExtractor.extractField(mockRequest, FREQUENCY_FIELD_NAME, messageError)).thenReturn(Optional.of(WRONG_FREQUENCY_STRING));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(WRONG_FREQUENCY, messageError);
    }

    @Test
    public void validate_WrongBatchBookingPreferredForBulk_shouldReturnError() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, BULK_PAYMENT_PATH_VAR);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

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
