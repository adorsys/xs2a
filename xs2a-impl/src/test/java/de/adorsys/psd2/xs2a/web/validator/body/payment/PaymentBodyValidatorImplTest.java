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
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.DateFieldValidator;
import de.adorsys.psd2.xs2a.web.validator.body.TppRedirectUriBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidatorContext;
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

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_NULL_VALUE;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE;
import static de.adorsys.psd2.xs2a.web.validator.body.payment.PaymentBodyValidatorImpl.FREQUENCY_FIELD_NAME;
import static de.adorsys.psd2.xs2a.web.validator.body.payment.PaymentBodyValidatorImpl.PURPOSE_CODE_FIELD_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentBodyValidatorImplTest {
    private static final String JSON_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAIN_PAYMENT_PRODUCT = "pain.001-sepa-credit-transfers";
    private static final String PAYMENT_SERVICE = "some payment service";
    private static final String INVALID_PAYMENT_SERVICE = "invalid payment service";
    private static final String PAYMENT_SERVICE_PATH_VAR = "payment-service";
    private static final String PAYMENT_PRODUCT_PATH_VAR = "payment-product";

    private static final MessageError DESERIALISATION_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_DESERIALIZATION_FAIL));
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
    private static final MessageError INVALID_FREQUENCY_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, FREQUENCY_FIELD_NAME));

    private PaymentBodyValidatorImpl validator;
    private MessageError messageError;

    @Mock
    private PaymentTypeValidatorContext paymentTypeValidatorContext;
    @Mock
    private StandardPaymentProductsResolver standardPaymentProductsResolver;
    @Mock
    private PaymentTypeValidator paymentTypeValidator;
    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;
    @Mock
    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;
    @Mock
    private DateFieldValidator dateFieldValidator;
    @Mock
    private FieldExtractor fieldExtractor;
    @Mock
    private PathParameterExtractor pathParameterExtractor;
    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        messageError = new MessageError(ErrorType.PIS_400);
        ErrorBuildingService errorService = new ErrorBuildingServiceMock(ErrorType.PIS_400);
        validator = new PaymentBodyValidatorImpl(errorService, xs2aObjectMapper, paymentTypeValidatorContext,
                                                 standardPaymentProductsResolver, tppRedirectUriBodyValidator,
                                                 dateFieldValidator, fieldExtractor, pathParameterExtractor);
    }

    @Test
    void validate_shouldExecuteSpecificValidator() throws IOException {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        Object paymentBody = new Object();
        when(xs2aObjectMapper.readValue(mockRequest.getInputStream(), Object.class))
            .thenReturn(paymentBody);

        when(paymentTypeValidatorContext.getValidator(PAYMENT_SERVICE))
            .thenReturn(Optional.of(paymentTypeValidator));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        verify(tppRedirectUriBodyValidator, times(1)).validate(mockRequest, messageError);
        verify(paymentTypeValidator).validate(paymentBody, messageError);
    }

    @Test
    void validate_wrongRequestBody_shouldReturnError() throws IOException {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(xs2aObjectMapper.readValue(mockRequest.getInputStream(), Object.class))
            .thenThrow(new IOException());

        // When
        validator.validate(mockRequest, messageError);

        // Then
        verify(tppRedirectUriBodyValidator, times(1)).validate(mockRequest, messageError);
        assertEquals(DESERIALISATION_ERROR, messageError);
    }

    @Test
    void validate_dayOfExecutionWrongValue_wrongFormat_error() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

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
    void validate_requestedExecutionDateCorrectValue_success() throws IOException {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        Object paymentBody = new Object();
        when(xs2aObjectMapper.readValue(mockRequest.getInputStream(), Object.class))
            .thenReturn(paymentBody);

        when(paymentTypeValidatorContext.getValidator(PAYMENT_SERVICE))
            .thenReturn(Optional.of(paymentTypeValidator));

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
    void validate_requestedExecutionTimeCorrectValue_success() throws IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        Object paymentBody = new Object();
        when(xs2aObjectMapper.readValue(mockRequest.getInputStream(), Object.class))
            .thenReturn(paymentBody);

        when(paymentTypeValidatorContext.getValidator(PAYMENT_SERVICE))
            .thenReturn(Optional.of(paymentTypeValidator));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }


    @Test
    void validate_rawPaymentProduct_shouldNotBeValidated() throws IOException {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(eq(PAIN_PAYMENT_PRODUCT)))
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
    void validate_unsupportedPaymentService_shouldThrowException() throws IOException {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, INVALID_PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        Object paymentBody = new Object();
        when(xs2aObjectMapper.readValue(mockRequest.getInputStream(), Object.class))
            .thenReturn(paymentBody);

        when(paymentTypeValidatorContext.getValidator(any()))
            .thenReturn(Optional.empty());

        // When
        assertThrows(IllegalArgumentException.class, () -> validator.validate(mockRequest, messageError));
        verify(tppRedirectUriBodyValidator, times(1)).validate(mockRequest, messageError);
    }

    @Test
    void validate_purposeCodes_shouldReturnError() {
        // Given
        String purposeCode = "CDCQ";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(xs2aObjectMapper.toJsonGetValuesForField(any(InputStream.class), anyString()))
            .thenReturn(Collections.singletonList(purposeCode));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(PURPOSE_CODE_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    void validate_noFrequency_shouldReturnError() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, "periodic-payments");
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(NO_FREQUENCY, messageError);
    }

    @Test
    void validate_invalidFrequency_shouldReturnError() {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, "periodic-payments");
        when(pathParameterExtractor.extractParameters(mockRequest)).thenReturn(templates);

        when(fieldExtractor.extractField(mockRequest, FREQUENCY_FIELD_NAME, messageError))
            .thenReturn(Optional.of("invalid value"));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(INVALID_FREQUENCY_ERROR, messageError);
    }

    private Map<String, String> buildTemplateVariables(String paymentProduct, String paymentService) {
        Map<String, String> templates = new HashMap<>();
        templates.put(PAYMENT_PRODUCT_PATH_VAR, paymentProduct);
        templates.put(PAYMENT_SERVICE_PATH_VAR, paymentService);
        return templates;
    }
}
