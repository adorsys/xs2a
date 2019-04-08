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

package de.adorsys.psd2.xs2a.web.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ErrorBuildingServiceTest {

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
    private ObjectMapper objectMapper;

    @Test
    public void buildErrorResponse_success() throws IOException {
        // Given
        when(serviceTypeDiscoveryService.getServiceType())
            .thenReturn(SERVICE_TYPE_PIS);
        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class)))
            .thenReturn(ERROR_PIS_400);
        when(errorMapperContainer.getErrorBody(any()))
            .thenReturn(null);
        when(objectMapper.writeValueAsString(any()))
            .thenReturn(RESPONSE_TEXT);

        ValidationResult validationResult = buildValidationResult();

        HttpServletResponse response = buildResponse();

        // When
        errorBuildingService.buildErrorResponse(response, validationResult.getMessageError());

        // Then
        MockHttpServletResponse actualResponse = (MockHttpServletResponse) response;
        String outputMessage = actualResponse.getContentAsString();

        assertNotNull(outputMessage);
        assertEquals(RESPONSE_TEXT, outputMessage);
        assertEquals(STATUS_CODE_400, actualResponse.getStatus());
        assertEquals(JSON, actualResponse.getContentType());
    }

    private HttpServletResponse buildResponse() {
        return new MockHttpServletResponse();
    }

    private ValidationResult buildValidationResult() {
        return ValidationResult.invalid(ERROR_PIS_400, MessageErrorCode.FORMAT_ERROR);
    }
}
