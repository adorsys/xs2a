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
    private static final String ENTRY_REFERENCE_FROM_PARAMETER_NAME = "entryReferenceFrom";
    private static final String DELTA_LIST_PARAMETER_NAME = "deltaList";

    private static final String DATE_FROM_TEST = LocalDate.now().toString();
    private static final String ENTRY_REFERENCE_TEST = "IVHdQ15dIOr0uQzvlCxLSB";
    private static final String DELTA_LIST_TEST = "true";

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
