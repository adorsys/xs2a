/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.validator.query.account;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingStatusQueryParameterValidatorImplTest {
    private static final String BOOKING_STATUS_PARAMETER_NAME = "bookingStatus";
    private static final MessageError MISSING_VALUE_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_ABSENT_PARAMETER, BOOKING_STATUS_PARAMETER_NAME));
    private static final MessageError BLANK_VALUE_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_BLANK_PARAMETER, BOOKING_STATUS_PARAMETER_NAME));
    private static final TppMessageInformation TPP_MESSAGE_INFORMATION =
        TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_INVALID_PARAMETER_VALUE, BOOKING_STATUS_PARAMETER_NAME);
    private static final MessageError INVALID_VALUE_ERROR =
        new MessageError(ErrorType.AIS_400, TPP_MESSAGE_INFORMATION);

    @InjectMocks
    private BookingStatusQueryParameterParamsValidatorImpl bookingStatusValidator;
    @Mock
    private ErrorBuildingService errorBuildingService;
    @Mock
    private MessageError messageError;

    private Map<String, List<String>> queryParams = new HashMap<>();

    @Test
    void validate_withCorrectValue_shouldNotEnrichError() {
        // Given
        queryParams.put(BOOKING_STATUS_PARAMETER_NAME, Collections.singletonList("booked"));

        // When
        bookingStatusValidator.validate(queryParams, messageError);

        // Then
        verify(errorBuildingService, never()).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(MessageError.class));
    }

    @Test
    void validate_withMissingParameter_shouldEnrichError() {
        // Given
        when(errorBuildingService.buildErrorType()).thenReturn(ErrorType.AIS_400);

        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);

        // When
        bookingStatusValidator.validate(queryParams, messageError);

        // Then
        verify(errorBuildingService).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService).enrichMessageError(eq(messageError), messageErrorCaptor.capture());

        assertEquals(MISSING_VALUE_ERROR, messageErrorCaptor.getValue());
    }

    @Test
    void validate_withBlankValue_shouldEnrichError() {
        // Given
        when(errorBuildingService.buildErrorType()).thenReturn(ErrorType.AIS_400);

        queryParams.put(BOOKING_STATUS_PARAMETER_NAME, Collections.singletonList(""));
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);

        // When
        bookingStatusValidator.validate(queryParams, messageError);

        // Then
        verify(errorBuildingService).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService).enrichMessageError(eq(messageError), messageErrorCaptor.capture());

        assertEquals(BLANK_VALUE_ERROR, messageErrorCaptor.getValue());
    }

    @Test
    void validate_withInvalidValue_shouldEnrichError() {
        // Given
        queryParams.put(BOOKING_STATUS_PARAMETER_NAME, Collections.singletonList("invalid value"));
        ArgumentCaptor<TppMessageInformation> errorTextCaptor = ArgumentCaptor.forClass(TppMessageInformation.class);

        // When
        bookingStatusValidator.validate(queryParams, messageError);

        // Then
        verify(errorBuildingService, never()).buildErrorType();
        verify(errorBuildingService, times(1)).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(MessageError.class));
        verify(errorBuildingService).enrichMessageError(eq(messageError), errorTextCaptor.capture());

        assertEquals(TPP_MESSAGE_INFORMATION, errorTextCaptor.getValue());
    }

    @Test
    void validate_withMultipleValues_shouldEnrichError() {
        // Given
        when(errorBuildingService.buildErrorType()).thenReturn(ErrorType.AIS_400);

        queryParams.put(BOOKING_STATUS_PARAMETER_NAME, Arrays.asList("booked", "pending"));
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);

        // When
        bookingStatusValidator.validate(queryParams, messageError);

        // Then
        verify(errorBuildingService).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService).enrichMessageError(eq(messageError), messageErrorCaptor.capture());

        assertEquals(INVALID_VALUE_ERROR, messageErrorCaptor.getValue());
    }
}
