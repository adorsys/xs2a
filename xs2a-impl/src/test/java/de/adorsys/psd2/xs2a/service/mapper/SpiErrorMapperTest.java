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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseToServiceAndErrorTypeMapper;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpiErrorMapperTest {
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(null, "777");

    @InjectMocks
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiResponseStatusToXs2aMessageErrorCodeMapper spiToXs2aMessageErrorCodeMapper;
    @Mock
    private SpiResponseToServiceAndErrorTypeMapper spiToServiceAndErrorTypeMapper;

    @Test
    public void mapToErrorHolder() {
        //Given:
        String message = "error";
        MessageErrorCode messageErrorCode = MessageErrorCode.PSU_CREDENTIALS_INVALID;
        ErrorType errorType = ErrorType.PIS_401;
        SpiResponse spiResponse = new SpiResponse<>(null, ASPSP_CONSENT_DATA, SpiResponseStatus.UNAUTHORIZED_FAILURE, Collections.singletonList(message));
        when(spiToXs2aMessageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus())).thenReturn(messageErrorCode);
        when(spiToServiceAndErrorTypeMapper.mapToErrorType(spiResponse.getResponseStatus(), ServiceType.PIS)).thenReturn(errorType);
        //When:
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
        //Then:
        assertNotNull(errorHolder);
        assertEquals(errorHolder.getErrorCode(), messageErrorCode);
        assertEquals(errorHolder.getErrorType(), errorType);
        assertEquals(errorHolder.getMessage(), message);
    }
}
