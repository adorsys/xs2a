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

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class SpiErrorMapperTest {
    @InjectMocks
    private SpiErrorMapper spiErrorMapper;

    @Test
    public void mapToErrorHolder() {
        // Given
        String message = "error";
        MessageErrorCode messageErrorCode = MessageErrorCode.PSU_CREDENTIALS_INVALID;
        ErrorType errorType = ErrorType.PIS_401;
        SpiResponse spiResponse = SpiResponse.builder()
                                      .error(new TppMessage(messageErrorCode, message))
                                      .build();
        TppMessageInformation expectedTppMessage = TppMessageInformation.of(messageErrorCode, message);

        // When
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);

        // Then
        assertNotNull(errorHolder);
        assertEquals(Collections.singletonList(expectedTppMessage), errorHolder.getTppMessageInformationList());
        assertEquals(errorType, errorHolder.getErrorType());
    }

    @Test
    public void mapToErrorHolder_withoutExplicitErrorsInSpiResponse() {
        // Given
        MessageErrorCode messageErrorCode = MessageErrorCode.PSU_CREDENTIALS_INVALID;
        ErrorType errorType = ErrorType.PIS_401;
        SpiResponse spiResponse = buildSpiResponseTransactionStatus();

        // When
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);

        // Then
        assertNotNull(errorHolder);
        assertEquals(messageErrorCode, errorHolder.getTppMessageInformationList().iterator().next().getMessageErrorCode());
        assertEquals(errorType, errorHolder.getErrorType());
    }

    @Test
    public void mapToErrorHolder_withMultipleErrorsInResponse() {
        // Given
        ErrorType firstErrorType = ErrorType.PIS_400;
        String firstMessage = "first error";
        MessageErrorCode firstErrorCode = MessageErrorCode.FORMAT_ERROR;
        TppMessage firstError = new TppMessage(firstErrorCode, firstMessage);


        MessageErrorCode secondErrorCode = MessageErrorCode.CANCELLATION_INVALID;
        String secondMessage = "second error";
        TppMessage secondError = new TppMessage(secondErrorCode, secondMessage);

        SpiResponse spiResponse = SpiResponse.builder()
                                      .error(Arrays.asList(firstError, secondError))
                                      .build();

        TppMessageInformation expectedFirstTppMessage = TppMessageInformation.of(firstErrorCode, firstMessage);
        TppMessageInformation expectedSecondTppMessage = TppMessageInformation.of(secondErrorCode, secondMessage);

        // When
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);

        // Then
        assertNotNull(errorHolder);
        assertEquals(Arrays.asList(expectedFirstTppMessage, expectedSecondTppMessage), errorHolder.getTppMessageInformationList());
        assertEquals(firstErrorType, errorHolder.getErrorType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mapToErrorHolder_withoutErrors_shouldThrowIllegalArgumentException() {
        // Given
        SpiResponse spiResponse = SpiResponse.<String>builder().payload("some payload").build();

        // When
        spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
    }

    private static SpiResponse<Void> buildSpiResponseTransactionStatus() {
        return SpiResponse.<Void>builder()
                   .error(new TppMessage(MessageErrorCode.PSU_CREDENTIALS_INVALID, "Unauthorised"))
                   .build();
    }

}
