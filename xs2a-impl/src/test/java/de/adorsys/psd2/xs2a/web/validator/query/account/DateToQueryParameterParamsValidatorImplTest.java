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
