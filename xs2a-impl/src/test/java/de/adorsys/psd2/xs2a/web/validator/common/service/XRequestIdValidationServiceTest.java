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

package de.adorsys.psd2.xs2a.web.validator.common.service;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class XRequestIdValidationServiceTest {

    private static final String CORRECT_UUID = "2f77a125-aa7a-45c0-b414-cea25a116039";
    private static final String WRONG_UUID = "qs2f77a125-aa7a-45c0-b414-cea25a116039";
    private static final int FORMAT_ERROR_CODE = MessageErrorCode.FORMAT_ERROR.getCode();
    private static final ErrorType ERROR_TYPE_PIS_400 = ErrorType.PIS_400;
    private static final ServiceType SERVICE_TYPE_PIS = ServiceType.PIS;

    @InjectMocks
    private XRequestIdValidationService xRequestIdValidationService;

    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;

    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;

    @Test
    public void validateXRequestId_success() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType())
            .thenReturn(SERVICE_TYPE_PIS);

        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class)))
            .thenReturn(ERROR_TYPE_PIS_400);

        // When
        ValidationResult actual = xRequestIdValidationService.validateXRequestId(CORRECT_UUID);

        // Then
        assertNotNull(actual);
        assertNull(actual.getMessageError());
    }

    @Test
    public void validateXRequestIdWrongUUID_fail() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType())
            .thenReturn(SERVICE_TYPE_PIS);

        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class)))
            .thenReturn(ERROR_TYPE_PIS_400);

        // When
        ValidationResult actual = xRequestIdValidationService.validateXRequestId(WRONG_UUID);

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getMessageError());
        assertEquals(FORMAT_ERROR_CODE, actual.getMessageError().getTppMessage().getMessageErrorCode().getCode());
    }

    @Test
    public void validateXRequestIdNullUUID_fail() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType())
            .thenReturn(SERVICE_TYPE_PIS);

        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class)))
            .thenReturn(ERROR_TYPE_PIS_400);

        // When
        ValidationResult actual = xRequestIdValidationService.validateXRequestId(null);

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getMessageError());
        assertEquals(FORMAT_ERROR_CODE, actual.getMessageError().getTppMessage().getMessageErrorCode().getCode());
    }
}
