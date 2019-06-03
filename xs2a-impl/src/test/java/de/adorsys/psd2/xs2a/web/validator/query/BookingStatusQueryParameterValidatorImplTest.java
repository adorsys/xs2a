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

package de.adorsys.psd2.xs2a.web.validator.query;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.query.account.BookingStatusQueryParameterParamsValidatorImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BookingStatusQueryParameterValidatorImplTest {
    private static final String BOOKING_STATUS_PARAMETER_NAME = "bookingStatus";
    private static final MessageError MISSING_VALUE_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, "Query parameter 'bookingStatus' is missing in request"));
    private static final MessageError BLANK_VALUE_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, "Query parameter 'bookingStatus' should not be blank"));
    private static final String INVALID_VALUE_ERROR_TEXT = "Query parameter 'bookingStatus' has invalid value";
    private static final MessageError INVALID_VALUE_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, INVALID_VALUE_ERROR_TEXT));

    @InjectMocks
    private BookingStatusQueryParameterParamsValidatorImpl bookingStatusValidator;
    @Mock
    private ErrorBuildingService errorBuildingService;
    @Mock
    private MessageError messageError;

    private Map<String, List<String>> queryParams = new HashMap<>();

    @Before
    public void setUp() {
        when(errorBuildingService.buildErrorType())
            .thenReturn(ErrorType.AIS_400);
    }

    @Test
    public void validate_withCorrectValue_shouldNotEnrichError() {
        // Given
        queryParams.put(BOOKING_STATUS_PARAMETER_NAME, Collections.singletonList("booked"));

        // When
        bookingStatusValidator.validate(queryParams, messageError);

        // Then
        verify(errorBuildingService, never()).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(MessageError.class));
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(String.class));
    }

    @Test
    public void validate_withMissingParameter_shouldEnrichError() {
        // Given
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);

        // When
        bookingStatusValidator.validate(queryParams, messageError);

        // Then
        verify(errorBuildingService).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService).enrichMessageError(eq(messageError), messageErrorCaptor.capture());
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(String.class));

        assertEquals(MISSING_VALUE_ERROR, messageErrorCaptor.getValue());
    }

    @Test
    public void validate_withBlankValue_shouldEnrichError() {
        // Given
        queryParams.put(BOOKING_STATUS_PARAMETER_NAME, Collections.singletonList(""));
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);

        // When
        bookingStatusValidator.validate(queryParams, messageError);

        // Then
        verify(errorBuildingService).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService).enrichMessageError(eq(messageError), messageErrorCaptor.capture());
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(String.class));

        assertEquals(BLANK_VALUE_ERROR, messageErrorCaptor.getValue());
    }

    @Test
    public void validate_withInvalidValue_shouldEnrichError() {
        // Given
        queryParams.put(BOOKING_STATUS_PARAMETER_NAME, Collections.singletonList("invalid value"));
        ArgumentCaptor<String> errorTextCaptor = ArgumentCaptor.forClass(String.class);

        // When
        bookingStatusValidator.validate(queryParams, messageError);

        // Then
        verify(errorBuildingService, never()).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(MessageError.class));
        verify(errorBuildingService).enrichMessageError(eq(messageError), errorTextCaptor.capture());

        assertEquals(INVALID_VALUE_ERROR_TEXT, errorTextCaptor.getValue());
    }

    @Test
    public void validate_withMultipleValues_shouldEnrichError() {
        // Given
        queryParams.put(BOOKING_STATUS_PARAMETER_NAME, Arrays.asList("booked", "pending"));
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);

        // When
        bookingStatusValidator.validate(queryParams, messageError);

        // Then
        verify(errorBuildingService).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService).enrichMessageError(eq(messageError), messageErrorCaptor.capture());
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(String.class));

        assertEquals(INVALID_VALUE_ERROR, messageErrorCaptor.getValue());
    }
}
