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

package de.adorsys.psd2.xs2a.web.interceptor.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.path.PaymentTypeAndProductValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PRODUCT_UNKNOWN_WRONG_PAYMENT_PRODUCT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentParametersValidationInterceptorTest {

    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";

    private Map<String, String> pathParameters = getPisRequestsPathParameters();

    @InjectMocks
    private PaymentParametersValidationInterceptor paymentParametersValidationInterceptor;

    @Mock
    private PaymentTypeAndProductValidator paymentTypeAndProductValidator;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HandlerMethod handler;
    @Mock
    private PathParameterExtractor pathParameterExtractor;
    @Mock
    private ErrorBuildingService errorBuildingService;

    String _initiatePayment() {
        return "OK";
    }

    @Test
    void validatePaymentInitiationParams_ok() throws IOException, NoSuchMethodException {
        // Given
        Method method = getClass().getDeclaredMethod("_initiatePayment");

        when(paymentTypeAndProductValidator.validateTypeAndProduct(PaymentType.SINGLE, CORRECT_PAYMENT_PRODUCT))
            .thenReturn(ValidationResult.valid());
        when(handler.getMethod())
            .thenReturn(method);
        when(pathParameterExtractor.extractParameters(any(HttpServletRequest.class)))
            .thenReturn(pathParameters);

        // When
        boolean actual = paymentParametersValidationInterceptor.preHandle(request, response, handler);

        // Then
        assertTrue(actual);
        verify(errorBuildingService, never()).buildFormatErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    void validatePaymentInitiationParams_ok_not_handler() throws IOException {
        // Given
        // When
        boolean actual = paymentParametersValidationInterceptor.preHandle(request, response, new Object());

        // Then
        assertTrue(actual);
        verify(errorBuildingService, never()).buildFormatErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    void validatePaymentInitiationParams_wrongType() throws IOException, NoSuchMethodException {
        // Given
        Method method = getClass().getDeclaredMethod("_initiatePayment");
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);

        when(handler.getMethod())
            .thenReturn(method);
        when(pathParameterExtractor.extractParameters(any(HttpServletRequest.class)))
            .thenReturn(pathParameters);

        pathParameters.put("payment-service", "payments-13432542542");

        // When
        boolean actual = paymentParametersValidationInterceptor.preHandle(request, response, handler);

        // Then
        assertFalse(actual);
        verify(errorBuildingService, times(1)).buildPaymentErrorResponse(eq(response), messageErrorCaptor.capture());
    }

    @Test
    void validatePaymentInitiationParams_serviceNull() throws IOException, NoSuchMethodException {
        // Given
        Method method = getClass().getDeclaredMethod("_initiatePayment");
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);

        when(handler.getMethod())
            .thenReturn(method);
        when(pathParameterExtractor.extractParameters(any(HttpServletRequest.class)))
            .thenReturn(pathParameters);

        pathParameters.remove("payment-service");

        // When
        boolean actual = paymentParametersValidationInterceptor.preHandle(request, response, handler);

        // Then
        assertFalse(actual);
        verify(errorBuildingService, times(1)).buildPaymentErrorResponse(eq(response), messageErrorCaptor.capture());
    }

    @Test
    void validatePaymentInitiationParams_unsupportedType() throws IOException, NoSuchMethodException {
        // Given
        Method method = getClass().getDeclaredMethod("_initiatePayment");
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);

        when(paymentTypeAndProductValidator.validateTypeAndProduct(PaymentType.PERIODIC, CORRECT_PAYMENT_PRODUCT))
            .thenReturn(ValidationResult.invalid(ErrorType.PIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE, "BULK")));
        when(handler.getMethod())
            .thenReturn(method);
        when(pathParameterExtractor.extractParameters(any(HttpServletRequest.class)))
            .thenReturn(pathParameters);

        pathParameters.put("payment-service", "periodic-payments");

        // When
        boolean actual = paymentParametersValidationInterceptor.preHandle(request, response, handler);

        // Then
        assertFalse(actual);
        verify(errorBuildingService, times(1)).buildPaymentErrorResponse(eq(response), messageErrorCaptor.capture());

    }

    @Test
    void validatePaymentInitiationParams_wrongProduct() throws IOException, NoSuchMethodException {
        // Given
        Method method = getClass().getDeclaredMethod("_initiatePayment");
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);

        when(paymentTypeAndProductValidator.validateTypeAndProduct(PaymentType.SINGLE, WRONG_PAYMENT_PRODUCT))
            .thenReturn(ValidationResult.invalid(ErrorType.PIS_404, TppMessageInformation.of(PRODUCT_UNKNOWN_WRONG_PAYMENT_PRODUCT, WRONG_PAYMENT_PRODUCT)));
        when(handler.getMethod())
            .thenReturn(method);
        when(pathParameterExtractor.extractParameters(any(HttpServletRequest.class)))
            .thenReturn(pathParameters);

        pathParameters.put("payment-product", WRONG_PAYMENT_PRODUCT);

        // When
        boolean actual = paymentParametersValidationInterceptor.preHandle(request, response, handler);

        // Then
        assertFalse(actual);
        verify(errorBuildingService, times(1)).buildPaymentErrorResponse(eq(response), messageErrorCaptor.capture());
    }

    private Map<String, String> getPisRequestsPathParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("payment-service", "payments");
        parameters.put("payment-product", CORRECT_PAYMENT_PRODUCT);

        return parameters;
    }
}
