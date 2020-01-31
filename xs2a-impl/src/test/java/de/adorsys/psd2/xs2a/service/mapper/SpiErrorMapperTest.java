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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SpiErrorMapperTest {
    @InjectMocks
    private SpiErrorMapper spiErrorMapper;

    @Test
    void mapToErrorHolder_WithCustomError() {
        // Given
        String message = "error message";
        ErrorType errorType = ErrorType.PIS_401;
        SpiResponse spiResponse = SpiResponse.builder()
                                      .error(new TppMessage(PSU_CREDENTIALS_INVALID, message))
                                      .build();
        TppMessageInformation expectedTppMessage = TppMessageInformation.buildWithCustomError(PSU_CREDENTIALS_INVALID, message);

        // When
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);

        // Then
        assertNotNull(errorHolder);
        assertEquals(Collections.singletonList(expectedTppMessage), errorHolder.getTppMessageInformationList());
        assertEquals(errorType, errorHolder.getErrorType());
    }

    @Test
    void mapToErrorHolder_WithBundleError() {
        // Given
        ErrorType errorType = ErrorType.PIS_401;
        SpiResponse spiResponse = SpiResponse.builder()
                                      .error(new TppMessage(PSU_CREDENTIALS_INVALID))
                                      .build();
        TppMessageInformation expectedTppMessage = TppMessageInformation.of(PSU_CREDENTIALS_INVALID);

        // When
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);

        // Then
        assertNotNull(errorHolder);
        assertEquals(Collections.singletonList(expectedTppMessage), errorHolder.getTppMessageInformationList());
        assertEquals(errorType, errorHolder.getErrorType());
    }

    @Test
    void mapToErrorHolder_WithBundleErrorAndMessageTextArgs() {
        // Given
        Object[] messageTextArgs = new Object[]{"parameter"};
        ErrorType errorType = ErrorType.PIS_401;
        SpiResponse spiResponse = SpiResponse.builder()
                                      .error(new TppMessage(PSU_CREDENTIALS_INVALID, messageTextArgs))
                                      .build();
        TppMessageInformation expectedTppMessage = TppMessageInformation.of(PSU_CREDENTIALS_INVALID, messageTextArgs);

        // When
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);

        // Then
        assertNotNull(errorHolder);
        assertEquals(Collections.singletonList(expectedTppMessage), errorHolder.getTppMessageInformationList());
        assertEquals(errorType, errorHolder.getErrorType());
    }

    @Test
    void mapToErrorHolder_withoutExplicitErrorsInSpiResponse() {
        // Given
        ErrorType errorType = ErrorType.PIS_401;
        SpiResponse spiResponse = buildSpiResponseTransactionStatus();

        // When
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);

        // Then
        assertNotNull(errorHolder);
        assertEquals(PSU_CREDENTIALS_INVALID, errorHolder.getTppMessageInformationList().iterator().next().getMessageErrorCode());
        assertEquals(errorType, errorHolder.getErrorType());
    }

    @Test
    void mapToErrorHolder_withMultipleErrorsInResponse() {
        // Given
        ErrorType firstErrorType = ErrorType.PIS_400;
        TppMessage firstError = new TppMessage(FORMAT_ERROR);


        TppMessage secondError = new TppMessage(CANCELLATION_INVALID);

        SpiResponse spiResponse = SpiResponse.builder()
                                      .error(Arrays.asList(firstError, secondError))
                                      .build();

        TppMessageInformation expectedFirstTppMessage = TppMessageInformation.of(FORMAT_ERROR);
        TppMessageInformation expectedSecondTppMessage = TppMessageInformation.of(CANCELLATION_INVALID);

        // When
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);

        // Then
        assertNotNull(errorHolder);
        assertEquals(Arrays.asList(expectedFirstTppMessage, expectedSecondTppMessage), errorHolder.getTppMessageInformationList());
        assertEquals(firstErrorType, errorHolder.getErrorType());
    }

    @Test
    void mapToErrorHolder_withoutErrors_shouldThrowIllegalArgumentException() {
        // Given
        SpiResponse spiResponse = SpiResponse.<String>builder().payload("some payload").build();

        // When
        assertThrows(IllegalArgumentException.class, () -> spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS));
    }

    private static SpiResponse<Void> buildSpiResponseTransactionStatus() {
        return SpiResponse.<Void>builder()
                   .error(new TppMessage(PSU_CREDENTIALS_INVALID))
                   .build();
    }

}
