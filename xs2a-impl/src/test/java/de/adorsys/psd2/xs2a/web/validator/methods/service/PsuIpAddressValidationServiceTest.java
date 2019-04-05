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

package de.adorsys.psd2.xs2a.web.validator.methods.service;

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
public class PsuIpAddressValidationServiceTest {

    private static final String CORRECT_IP_ADDRESS = "192.168.7.40";
    private static final String CORRECT_V6_IP_ADDRESS = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
    private static final String WRONG_IP_ADDRESS = "683.168.11.56";
    private static final String WRONG_V6_IP_ADDRESS = "2001:0db8:85a3:0000:0n0k:8a2e:0370:7334";
    private static final ErrorType ERROR_TYPE_PIS_400 = ErrorType.PIS_400;
    private static final ServiceType SERVICE_TYPE_PIS = ServiceType.PIS;
    private static final int FORMAT_ERROR_CODE = MessageErrorCode.FORMAT_ERROR.getCode();

    @InjectMocks
    private PsuIpAddressValidationService psuIpAddressValidationService;

    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;

    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;

    @Test
    public void validatePsuIdAddress_success() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType())
            .thenReturn(SERVICE_TYPE_PIS);

        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class)))
            .thenReturn(ERROR_TYPE_PIS_400);

        // When
        ValidationResult actual = psuIpAddressValidationService.validatePsuIdAddress(CORRECT_IP_ADDRESS);

        // Then
        assertNotNull(actual);
        assertNull(actual.getMessageError());
    }

    @Test
    public void validatePsuIdAddressV6_success() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType())
            .thenReturn(SERVICE_TYPE_PIS);

        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class)))
            .thenReturn(ERROR_TYPE_PIS_400);

        // When
        ValidationResult actual = psuIpAddressValidationService.validatePsuIdAddress(CORRECT_V6_IP_ADDRESS);

        // Then
        assertNotNull(actual);
        assertNull(actual.getMessageError());
    }

    @Test
    public void validatePsuIdAddressWrongIPAddress_fail() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType())
            .thenReturn(SERVICE_TYPE_PIS);

        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class)))
            .thenReturn(ERROR_TYPE_PIS_400);

        // When
        ValidationResult actual = psuIpAddressValidationService.validatePsuIdAddress(WRONG_IP_ADDRESS);

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getMessageError());
        assertEquals(FORMAT_ERROR_CODE, actual.getMessageError().getTppMessage().getMessageErrorCode().getCode());
    }

    @Test
    public void validatePsuIdAddressWrongIPAddressV6_fail() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType())
            .thenReturn(SERVICE_TYPE_PIS);

        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class)))
            .thenReturn(ERROR_TYPE_PIS_400);

        // When
        ValidationResult actual = psuIpAddressValidationService.validatePsuIdAddress(WRONG_V6_IP_ADDRESS);

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getMessageError());
        assertEquals(FORMAT_ERROR_CODE, actual.getMessageError().getTppMessage().getMessageErrorCode().getCode());
    }

    @Test
    public void validatePsuIdAddressNullIPAddress_fail() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType())
            .thenReturn(SERVICE_TYPE_PIS);

        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class)))
            .thenReturn(ERROR_TYPE_PIS_400);

        // When
        ValidationResult actual = psuIpAddressValidationService.validatePsuIdAddress(null);

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getMessageError());
        assertEquals(FORMAT_ERROR_CODE, actual.getMessageError().getTppMessage().getMessageErrorCode().getCode());
    }

}
