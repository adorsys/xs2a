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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.web.converter.LocalDateConverter;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.DateFieldValidator;
import de.adorsys.psd2.xs2a.web.validator.body.TppRedirectUriBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.type.PaymentTypeValidatorContext;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentBodyValidatorImplTest {

    private static final String JSON_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAIN_PAYMENT_PRODUCT = "pain.001-sepa-credit-transfers";
    private static final String PAYMENT_SERVICE = "some payment service";
    private static final String INVALID_PAYMENT_SERVICE = "invalid payment service";
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

    private static final MessageError DESERIALISATION_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, "Cannot deserialize the request body"));
    private static final MessageError DAY_OF_EXECUTION_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, "Value 'dayOfExecution' should be a number of day in month"));
    private static final MessageError REQUESTED_EXECUTION_DATE_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, "Wrong format for 'requestedExecutionDate': value should be ISO_DATE 'YYYY-MM-DD' format."));
    private static final MessageError REQUESTED_EXECUTION_TIME_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, "Wrong format for 'requestedExecutionTime': value should be ISO_DATE_TIME 'YYYY-MM-DD'T'HH:mm:ssZ' format."));

    private static final MessageError PURPOSE_CODE_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, PaymentBodyValidatorImpl.PURPOSE_CODE_ERROR_FORMAT));

    private PaymentBodyValidatorImpl validator;
    private MessageError messageError;

    @Mock
    private PaymentTypeValidatorContext paymentTypeValidatorContext;
    @Mock
    private StandardPaymentProductsResolver standardPaymentProductsResolver;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private PaymentTypeValidator paymentTypeValidator;
    @Mock
    private JsonConverter jsonConverter;
    @Mock
    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;
    private MockHttpServletRequest mockRequest;

    @Before
    public void setUp() {
        mockRequest = new MockHttpServletRequest();
        messageError = new MessageError(ErrorType.PIS_400);
        ErrorBuildingService errorService = new ErrorBuildingServiceMock(ErrorType.PIS_400);
        validator = new PaymentBodyValidatorImpl(errorService, objectMapper, paymentTypeValidatorContext,
                                                 standardPaymentProductsResolver, jsonConverter, tppRedirectUriBodyValidator, new DateFieldValidator(errorService, jsonConverter, new LocalDateConverter()));
        when(standardPaymentProductsResolver.isRawPaymentProduct(eq(PAIN_PAYMENT_PRODUCT)))
            .thenReturn(true);
        when(standardPaymentProductsResolver.isRawPaymentProduct(eq(JSON_PAYMENT_PRODUCT)))
            .thenReturn(false);
    }

    @Test
    public void validate_shouldExecuteSpecificValidator() throws IOException {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);

        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        doNothing().when(tppRedirectUriBodyValidator).validate(mockRequest, messageError);
        Object paymentBody = new Object();
        when(objectMapper.readValue(mockRequest.getInputStream(), Object.class))
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
    public void validate_wrongRequestBody_shouldReturnError() throws IOException {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        doNothing().when(tppRedirectUriBodyValidator).validate(mockRequest, messageError);
        when(objectMapper.readValue(mockRequest.getInputStream(), Object.class))
            .thenThrow(new IOException());

        // When
        validator.validate(mockRequest, messageError);

        // Then
        verify(tppRedirectUriBodyValidator, times(1)).validate(mockRequest, messageError);
        assertEquals(DESERIALISATION_ERROR, messageError);
    }

    @Test
    public void validate_dayOfExecutionWrongValue_wrongFormat_error() throws IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(jsonConverter.toJsonField(any(InputStream.class), eq(DAY_OF_EXECUTION_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(WRONG_DAY_OF_MONTH));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(DAY_OF_EXECUTION_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    public void validate_requestedExecutionDateWrongValue_wrongFormat_error() throws IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(jsonConverter.toJsonField(any(InputStream.class), eq(DAY_OF_EXECUTION_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));
        when(jsonConverter.toJsonField(any(InputStream.class), eq(REQUESTED_EXECUTION_DATE_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(WRONG_FORMAT_DATE));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(REQUESTED_EXECUTION_DATE_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    public void validate_requestedExecutionDateCorrectValue_success() throws IOException {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        doNothing().when(tppRedirectUriBodyValidator).validate(mockRequest, messageError);
        Object paymentBody = new Object();
        when(objectMapper.readValue(mockRequest.getInputStream(), Object.class))
            .thenReturn(paymentBody);

        when(jsonConverter.toJsonField(any(InputStream.class), eq(DAY_OF_EXECUTION_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));
        when(jsonConverter.toJsonField(any(InputStream.class), eq(REQUESTED_EXECUTION_DATE_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(CORRECT_FORMAT_DATE));

        when(paymentTypeValidatorContext.getValidator(PAYMENT_SERVICE))
            .thenReturn(Optional.of(paymentTypeValidator));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        verify(tppRedirectUriBodyValidator, times(1)).validate(mockRequest, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_requestedExecutionTimeWrongValue_wrongFormat_error() throws IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        when(jsonConverter.toJsonField(any(InputStream.class), eq(DAY_OF_EXECUTION_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));
        when(jsonConverter.toJsonField(any(InputStream.class), eq(REQUESTED_EXECUTION_TIME_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(WRONG_FORMAT_TIME));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(REQUESTED_EXECUTION_TIME_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    public void validate_requestedExecutionTimeCorrectValue_success() throws IOException {
        // Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        Object paymentBody = new Object();
        when(objectMapper.readValue(mockRequest.getInputStream(), Object.class))
            .thenReturn(paymentBody);

        when(jsonConverter.toJsonField(any(InputStream.class), eq(DAY_OF_EXECUTION_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(CORRECT_DAY_OF_MONTH));
        when(jsonConverter.toJsonField(any(InputStream.class), eq(REQUESTED_EXECUTION_TIME_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(CORRECT_FORMAT_TIME));

        when(paymentTypeValidatorContext.getValidator(PAYMENT_SERVICE))
            .thenReturn(Optional.of(paymentTypeValidator));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
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
        verify(objectMapper, never()).readValue(any(InputStream.class), any(Class.class));
        verify(tppRedirectUriBodyValidator, never()).validate(mockRequest, messageError);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_unsupportedPaymentService_shouldThrowException() throws IOException {
        // Given
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, INVALID_PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        doNothing().when(tppRedirectUriBodyValidator).validate(mockRequest, messageError);
        Object paymentBody = new Object();
        when(objectMapper.readValue(mockRequest.getInputStream(), Object.class))
            .thenReturn(paymentBody);

        when(paymentTypeValidatorContext.getValidator(any()))
            .thenReturn(Optional.empty());

        // When
        validator.validate(mockRequest, messageError);
        verify(tppRedirectUriBodyValidator, times(1)).validate(mockRequest, messageError);
    }

    @Test
    public void validate_purposeCodes_shouldReturnError() throws IOException {
        // Given
        String purposeCode = "CDCQ";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Map<String, String> templates = buildTemplateVariables(JSON_PAYMENT_PRODUCT, PAYMENT_SERVICE);
        mockRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, templates);

        Object paymentBody = new Object();
        when(jsonConverter.toJsonGetValuesForField(any(InputStream.class), anyString()))
            .thenReturn(Collections.singletonList(purposeCode));

        // When
        validator.validate(mockRequest, messageError);

        // Then
        assertEquals(PURPOSE_CODE_WRONG_VALUE_ERROR, messageError);
    }

    private Map<String, String> buildTemplateVariables(String paymentProduct, String paymentService) {
        Map<String, String> templates = new HashMap<>();
        templates.put(PAYMENT_PRODUCT_PATH_VAR, paymentProduct);
        templates.put(PAYMENT_SERVICE_PATH_VAR, paymentService);
        return templates;
    }
}
