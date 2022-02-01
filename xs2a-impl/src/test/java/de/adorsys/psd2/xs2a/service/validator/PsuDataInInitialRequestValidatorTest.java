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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PsuDataInInitialRequestValidatorTest {
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final PsuIdData PSU_DATA = new PsuIdData("some psu id", null, null, null, null);
    private static final MessageError BLANK_PSU_ID_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR_PSU_ID_BLANK));
    private static final MessageError NULL_PSU_ID_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR_NO_PSU_ID));
    private static final ServiceType SERVICE_TYPE = ServiceType.AIS;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;

    @InjectMocks
    private PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;

    @Test
    void validate_withPsuDataNotMandated_shouldReturnValid() {
        //When
        ValidationResult validationResult = psuDataInInitialRequestValidator.validate(EMPTY_PSU_DATA);

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_withPsuDataMandatedAndValidPsuData_shouldReturnValid() {
        //Given
        when(aspspProfileService.isPsuInInitialRequestMandated()).thenReturn(true);

        //When
        ValidationResult validationResult = psuDataInInitialRequestValidator.validate(PSU_DATA);

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_withPsuDataMandatedAndBlankPsuId_shouldReturnFormatError() {
        //Given
        when(aspspProfileService.isPsuInInitialRequestMandated()).thenReturn(true);

        PsuIdData psuIdData = new PsuIdData(" ", null, null, null, null);
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode())).thenReturn(ErrorType.AIS_400);

        //When
        ValidationResult validationResult = psuDataInInitialRequestValidator.validate(psuIdData);

        //Then
        verify(serviceTypeDiscoveryService).getServiceType();
        verify(errorTypeMapper).mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode());

        assertTrue(validationResult.isNotValid());
        assertEquals(BLANK_PSU_ID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withPsuDataMandatedAndNullPsuId_shouldReturnFormatError() {
        //Given
        when(aspspProfileService.isPsuInInitialRequestMandated()).thenReturn(true);
        PsuIdData psuIdData = new PsuIdData(null, null, null, null, null);

        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode())).thenReturn(ErrorType.AIS_400);

        //When
        ValidationResult validationResult = psuDataInInitialRequestValidator.validate(psuIdData);

        //Then
        verify(serviceTypeDiscoveryService).getServiceType();
        verify(errorTypeMapper).mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode());

        assertTrue(validationResult.isNotValid());
        assertEquals(NULL_PSU_ID_ERROR, validationResult.getMessageError());
    }

    @Test
    void buildWarningMessages() {
        //Given
        PsuIdData psuIdData = new PsuIdData(null, null, null, null, null);

        //When
        Set<TppMessageInformation> actual = psuDataInInitialRequestValidator.buildWarningMessages(psuIdData);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(aspspProfileService);
        verifyNoInteractions(requestProviderService);
        verifyNoInteractions(serviceTypeDiscoveryService);
        verifyNoInteractions(errorTypeMapper);
    }
}
