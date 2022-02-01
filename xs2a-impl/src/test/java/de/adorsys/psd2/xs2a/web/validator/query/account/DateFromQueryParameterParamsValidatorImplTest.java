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

package de.adorsys.psd2.xs2a.web.validator.query.account;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_ABSENT_PARAMETER;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_FIELD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DateFromQueryParameterParamsValidatorImplTest {
    private static final String DATE_FROM_PARAMETER_NAME = "dateFrom";
    private static final String BOOKING_STATUS_PARAMETER_NAME = "bookingStatus";
    private static final String ENTRY_REFERENCE_FROM_PARAMETER_NAME = "entryReferenceFrom";
    private static final String DELTA_LIST_PARAMETER_NAME = "deltaList";

    private static final String DATE_FROM_TEST = LocalDate.now().toString();
    private static final String ENTRY_REFERENCE_TEST = "IVHdQ15dIOr0uQzvlCxLSB";
    private static final String DELTA_LIST_TEST = "true";
    private static final String BOOKING_STATUS_INFORMATION = "information";
    private static final String BOOKING_STATUS_BOOKED = "booked";

    private Map<String, List<String>> queryParams = new HashMap<>();

    @InjectMocks
    private DateFromQueryParameterParamsValidatorImpl dateFromQueryParameterParamsValidator;
    @Mock
    private MessageError messageError;
    @Mock
    private ErrorBuildingService errorBuildingService;

    @Test
    void validate_withCorrectValue_shouldNotEnrichError() {
        //Given
        queryParams.put(DATE_FROM_PARAMETER_NAME, Collections.singletonList(DATE_FROM_TEST));
        //When
        dateFromQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verifyNoError();
    }

    @Test
    void validate_withoutDateFromAndInformationStatus_shouldNotEnrichError() {
        //Given
        queryParams.put(BOOKING_STATUS_PARAMETER_NAME, Collections.singletonList(BOOKING_STATUS_INFORMATION));
        //When
        dateFromQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verifyNoError();
    }

    @Test
    void validate_withCorrectValueAndEntryReferenceFromAndDeltaList_shouldNotEnrichError() {
        //Given
        queryParams.put(DATE_FROM_PARAMETER_NAME, Collections.singletonList(LocalDate.now().toString()));
        queryParams.put(ENTRY_REFERENCE_FROM_PARAMETER_NAME, Collections.singletonList(ENTRY_REFERENCE_TEST));
        queryParams.put(DELTA_LIST_PARAMETER_NAME, Collections.singletonList(DELTA_LIST_TEST));
        //When
        dateFromQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verifyNoError();
    }

    @Test
    void validate_withCorrectValueAndEntryReferenceFrom_shouldNotEnrichError() {
        //Given
        queryParams.put(DATE_FROM_PARAMETER_NAME, Collections.singletonList(LocalDate.now().toString()));
        queryParams.put(ENTRY_REFERENCE_FROM_PARAMETER_NAME, Collections.singletonList(ENTRY_REFERENCE_TEST));
        //When
        dateFromQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verifyNoError();
    }

    @Test
    void validate_withCorrectValueAndEntryDeltaList_shouldNotEnrichError() {
        //Given
        queryParams.put(DATE_FROM_PARAMETER_NAME, Collections.singletonList(LocalDate.now().toString()));
        queryParams.put(DELTA_LIST_PARAMETER_NAME, Collections.singletonList(DELTA_LIST_TEST));
        //When
        dateFromQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verifyNoError();
    }

    @Test
    void validate_withMissingParameterAndEntryReferenceFrom_shouldNotEnrichError() {
        //Given
        queryParams.put(ENTRY_REFERENCE_FROM_PARAMETER_NAME, Collections.singletonList(ENTRY_REFERENCE_TEST));
        //When
        dateFromQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verifyNoError();
    }

    @Test
    void validate_withMissingParameterAndEntryDeltaList_shouldNotEnrichError() {
        //Given
        queryParams.put(DELTA_LIST_PARAMETER_NAME, Collections.singletonList(DELTA_LIST_TEST));
        //When
        dateFromQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verifyNoError();
    }

    @Test
    void validate_withMissingParameter_shouldEnrichError() {
        //Given
        ArgumentCaptor<TppMessageInformation> captor = ArgumentCaptor.forClass(TppMessageInformation.class);
        //When
        dateFromQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verifyError(captor);
    }

    @Test
    void validate_withMissingParameterAndBookedStatus_shouldEnrichError() {
        //Given
        queryParams.put(BOOKING_STATUS_PARAMETER_NAME, Collections.singletonList(BOOKING_STATUS_BOOKED));
        ArgumentCaptor<TppMessageInformation> captor = ArgumentCaptor.forClass(TppMessageInformation.class);
        //When
        dateFromQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verifyError(captor);
    }

    @Test
    void validate_wrongFormatError() {
        //Given
        ArgumentCaptor<TppMessageInformation> captor = ArgumentCaptor.forClass(TppMessageInformation.class);
        queryParams.put(DATE_FROM_PARAMETER_NAME, Collections.singletonList("wrong_date_format"));
        //When
        dateFromQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verify(errorBuildingService, never()).buildErrorType();
        verify(errorBuildingService).enrichMessageError(eq(messageError), captor.capture());
        assertEquals(FORMAT_ERROR_INVALID_FIELD, captor.getValue().getMessageErrorCode());
    }

    private void verifyNoError() {
        verify(errorBuildingService, never()).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(MessageError.class));
    }

    private void verifyError(ArgumentCaptor<TppMessageInformation> captor) {
        verify(errorBuildingService, never()).buildErrorType();
        verify(errorBuildingService).enrichMessageError(eq(messageError), captor.capture());
        assertEquals(FORMAT_ERROR_ABSENT_PARAMETER, captor.getValue().getMessageErrorCode());
    }
}
