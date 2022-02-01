/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorBuildingServiceTest {

    private static final String RESPONSE_TEXT = "some response text";
    private static final int STATUS_CODE_400 = 400;
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final ServiceType SERVICE_TYPE_PIS = ServiceType.PIS;
    private static final ErrorType ERROR_PIS_400 = ErrorType.PIS_400;

    @InjectMocks
    private ErrorBuildingService errorBuildingService;

    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;
    @Mock
    private ErrorMapperContainer errorMapperContainer;
    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;
    private MockHttpServletResponse response;

    @Captor
    private ArgumentCaptor<MessageError> messageErrorCaptor;

    @BeforeEach
    void setUp() throws Exception {
        response = new MockHttpServletResponse();

        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE_PIS);
        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class))).thenReturn(ERROR_PIS_400);
        when(xs2aObjectMapper.writeValueAsString(any())).thenReturn(RESPONSE_TEXT);
        when(errorMapperContainer.getErrorBody(messageErrorCaptor.capture())).thenReturn(null);
    }

    @Test
    void buildErrorResponse_success() throws IOException {
        // Given
        ValidationResult validationResult = buildValidationResult(MessageErrorCode.FORMAT_ERROR);

        // When
        errorBuildingService.buildFormatErrorResponse(response, validationResult.getMessageError());

        // Then
        String outputMessage = response.getContentAsString();
        assertNotNull(outputMessage);
        assertEquals(RESPONSE_TEXT, outputMessage);
        assertEquals(STATUS_CODE_400, response.getStatus());
        assertEquals(JSON, response.getContentType());

        MessageError actualMessageError = messageErrorCaptor.getValue();
        assertEquals(ERROR_PIS_400, actualMessageError.getErrorType());
        assertEquals(1, actualMessageError.getTppMessages().size());
        assertEquals(MessageErrorCode.FORMAT_ERROR, actualMessageError.getTppMessages().iterator().next().getMessageErrorCode());
    }

    @Test
    void buildErrorResponse_executionDateInvalid() throws IOException {
        // Given
        ValidationResult validationResult = buildValidationResult(MessageErrorCode.EXECUTION_DATE_INVALID);

        // When
        errorBuildingService.buildFormatErrorResponse(response, validationResult.getMessageError());

        // Then
        String outputMessage = response.getContentAsString();
        assertNotNull(outputMessage);
        assertEquals(RESPONSE_TEXT, outputMessage);
        assertEquals(STATUS_CODE_400, response.getStatus());
        assertEquals(JSON, response.getContentType());
        assertEquals(JSON, response.getContentType());

        MessageError actualMessageError = messageErrorCaptor.getValue();
        assertEquals(ERROR_PIS_400, actualMessageError.getErrorType());
        assertEquals(1, actualMessageError.getTppMessages().size());
        assertEquals(MessageErrorCode.EXECUTION_DATE_INVALID, actualMessageError.getTppMessages().iterator().next().getMessageErrorCode());
    }

    private ValidationResult buildValidationResult(MessageErrorCode messageErrorCode) {
        return ValidationResult.invalid(ERROR_PIS_400, messageErrorCode);
    }
}
