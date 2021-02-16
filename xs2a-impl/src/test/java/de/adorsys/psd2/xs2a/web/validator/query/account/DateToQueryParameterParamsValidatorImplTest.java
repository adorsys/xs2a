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
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_DATE_PERIOD_INVALID;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_FIELD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DateToQueryParameterParamsValidatorImplTest {

    private static final String DATE_FROM_PARAMETER_NAME = "dateFrom";
    private static final String DATE_TO_PARAMETER_NAME = "dateTo";

    private Map<String, List<String>> queryParams;

    @InjectMocks
    private DateToQueryParameterParamsValidatorImpl dateToQueryParameterParamsValidator;
    @Mock
    private MessageError messageError;
    @Mock
    private ErrorBuildingService errorBuildingService;

    @BeforeEach
    void setUp() {
        queryParams = new HashMap<>();
    }

    @ParameterizedTest
    @MethodSource(value = "successQueryParameters")
    void validate_shouldNotEnrichError(String dateFrom, String dateTo) {
        //Given
        queryParams.put(DATE_FROM_PARAMETER_NAME, Collections.singletonList(dateFrom));
        queryParams.put(DATE_TO_PARAMETER_NAME, Collections.singletonList(dateTo));
        //When
        dateToQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verify(errorBuildingService, never()).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(MessageError.class));
    }

    @Test
    void validate_noParameters_shouldNotEnrichError() {
        //Given
        //When
        dateToQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verify(errorBuildingService, never()).buildErrorType();
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(TppMessageInformation.class));
        verify(errorBuildingService, never()).enrichMessageError(eq(messageError), any(MessageError.class));
    }

    private static Stream<Arguments> successQueryParameters() {
        LocalDate localDate = LocalDate.now();
        return Stream.of(Arguments.arguments(localDate.toString(), localDate.plusDays(1).toString()),
                         Arguments.arguments(localDate.toString(), localDate.toString()),
                         Arguments.arguments("wrong_format_date", localDate.toString())
        );
    }

    @ParameterizedTest
    @MethodSource(value = "failureQueryParameters")
    void validate_shouldEnrichError(String dateFrom, String dateTo, MessageErrorCode expectedErrorCode) {
        //Given
        ArgumentCaptor<TppMessageInformation> captor = ArgumentCaptor.forClass(TppMessageInformation.class);
        queryParams.put(DATE_FROM_PARAMETER_NAME, Collections.singletonList(dateFrom));
        queryParams.put(DATE_TO_PARAMETER_NAME, Collections.singletonList(dateTo));
        //When
        dateToQueryParameterParamsValidator.validate(queryParams, messageError);
        //Then
        verify(errorBuildingService, never()).buildErrorType();
        verify(errorBuildingService).enrichMessageError(eq(messageError), captor.capture());
        assertEquals(expectedErrorCode, captor.getValue().getMessageErrorCode());
    }

    private static Stream<Arguments> failureQueryParameters() {
        return Stream.of(Arguments.arguments(LocalDate.now().toString(), LocalDate.now().minusDays(1).toString(), FORMAT_ERROR_DATE_PERIOD_INVALID),
                         Arguments.arguments("", "", FORMAT_ERROR_INVALID_FIELD),
                         Arguments.arguments(LocalDate.now().toString(), "wrong_format_date", FORMAT_ERROR_INVALID_FIELD)
        );
    }
}
