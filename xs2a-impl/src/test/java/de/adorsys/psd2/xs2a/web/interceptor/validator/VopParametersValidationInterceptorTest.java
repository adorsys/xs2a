/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.interceptor.validator;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import de.adorsys.psd2.xs2a.web.Psd2VopMethodNameConstant;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.method.HandlerMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VopParametersValidationInterceptorTest {

    private static final String PARTY_VERIFICATION_ENDPOINT = "party-verification-endpoint";
    private static final String BULK_PARTY_VERIFICATION_ENDPOINT = "bulk-party-verification-endpoint";

    @Mock
    private ErrorBuildingService errorBuildingService;

    @Mock
    private PathParameterExtractor pathParameterExtractor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    @Mock
    private Method method;

    @InjectMocks
    private VopParametersValidationInterceptor interceptor;

    private Map<String, String> pathParameters;

    @BeforeEach
    void setUp() {
        pathParameters = new HashMap<>();
        when(pathParameterExtractor.extractParameters(request)).thenReturn(pathParameters);
    }

    @Test
    void preHandle_withValidPayeeVerifications_shouldReturnTrue() throws IOException {
        // Given
        pathParameters.put(PARTY_VERIFICATION_ENDPOINT, "payee-verifications");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.VERIFICATION_OF_PARTY.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }

    @Test
    void preHandle_withValidPayerVerifications_shouldReturnTrue() throws IOException {
        // Given
        pathParameters.put(PARTY_VERIFICATION_ENDPOINT, "payer-verifications");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.VERIFICATION_OF_PARTY.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }

    @Test
    void preHandle_withValidPartyVerifications_shouldReturnTrue() throws IOException {
        // Given
        pathParameters.put(PARTY_VERIFICATION_ENDPOINT, "party-verifications");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.VERIFICATION_OF_PARTY.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }

    @Test
    void preHandle_withInvalidPartyVerificationEndpoint_shouldReturnFalse() throws IOException {
        // Given
        pathParameters.put(PARTY_VERIFICATION_ENDPOINT, "invalid-endpoint");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.VERIFICATION_OF_PARTY.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(errorBuildingService).buildVopErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    void preHandle_withNullPartyVerificationEndpoint_shouldReturnFalse() throws IOException {
        // Given
        pathParameters.put(PARTY_VERIFICATION_ENDPOINT, null);
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.VERIFICATION_OF_PARTY.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(errorBuildingService).buildVopErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    void preHandle_withValidBulkPayeeVerifications_shouldReturnTrue() throws IOException {
        // Given
        pathParameters.put(BULK_PARTY_VERIFICATION_ENDPOINT, "bulk-payee-verifications");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.BULK_VERIFICATION_OF_PARTY.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }

    @Test
    void preHandle_withValidBulkPayerVerifications_shouldReturnTrue() throws IOException {
        // Given
        pathParameters.put(BULK_PARTY_VERIFICATION_ENDPOINT, "bulk-payer-verifications");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.BULK_VERIFICATION_OF_PARTY.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }

    @Test
    void preHandle_withValidBulkPartyVerifications_shouldReturnTrue() throws IOException {
        // Given
        pathParameters.put(BULK_PARTY_VERIFICATION_ENDPOINT, "bulk-party-verifications");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.BULK_VERIFICATION_OF_PARTY.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }

    @Test
    void preHandle_withInvalidBulkPartyVerificationEndpoint_shouldReturnFalse() throws IOException {
        // Given
        pathParameters.put(BULK_PARTY_VERIFICATION_ENDPOINT, "invalid-bulk-endpoint");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.BULK_VERIFICATION_OF_PARTY.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(errorBuildingService).buildVopErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    void preHandle_withGetBulkVerificationOfPartyAndValidEndpoint_shouldReturnTrue() throws IOException {
        // Given
        pathParameters.put(BULK_PARTY_VERIFICATION_ENDPOINT, "bulk-payee-verifications");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.GET_BULK_VERIFICATION_OF_PARTY.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }

    @Test
    void preHandle_withGetBulkVerificationOfPartyStatusAndValidEndpoint_shouldReturnTrue() throws IOException {
        // Given
        pathParameters.put(BULK_PARTY_VERIFICATION_ENDPOINT, "bulk-payer-verifications");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.GET_BULK_VERIFICATION_OF_PARTY_STATUS.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }

    @Test
    void preHandle_withCreditorConfirmation_shouldSkipValidation() throws IOException {
        // Given
        pathParameters.put("payment-service", "payments");
        pathParameters.put("payment-product", "sepa-credit-transfers");
        pathParameters.put("paymentId", "12345");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(Psd2VopMethodNameConstant.CREDITOR_CONFIRMATION.getValue());

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }

    @Test
    void preHandle_withNonVopRequest_shouldReturnTrue() throws IOException {
        // Given
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("_someOtherMethod");

        // When
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }

    @Test
    void preHandle_withNonHandlerMethodObject_shouldReturnTrue() throws IOException {
        // Given
        Object nonHandlerMethod = new Object();

        // When
        boolean result = interceptor.preHandle(request, response, nonHandlerMethod);

        // Then
        assertTrue(result);
        verify(errorBuildingService, never()).buildVopErrorResponse(any(), any());
    }
}
