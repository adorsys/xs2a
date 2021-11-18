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

package de.adorsys.psd2.xs2a.web.validator.body;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.converter.LocalDateConverter;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aBodyDateFormatter;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateField;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_DAY_OF_EXECUTION;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_MONTHS_OF_EXECUTION;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_SIZE_MONTHS_OF_EXECUTION;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD;


@Component
@RequiredArgsConstructor
public class DateFieldValidator {
    private static final String DAY_OF_EXECUTION_FIELD_NAME = "dayOfExecution";
    private static final String DAY_OF_MONTH_REGEX = "(0?[1-9]|[12]\\d|3[01])";
    private static final String MONTHS_OF_EXECUTION_FIELD_NAME = "monthsOfExecution";
    private static final String MONTH_OF_YEAR_REGEX = "^([1-9]|1[012])$";

    private final ErrorBuildingService errorBuildingService;
    private final LocalDateConverter localDateConverter;
    private final FieldExtractor fieldExtractor;

    public MessageError validateDateFormat(HttpServletRequest request, Set<Xs2aRequestBodyDateField> fields, MessageError messageError) {
        return validateRawDataDates(request, fields, messageError);
    }

    public void validateDayOfExecution(HttpServletRequest request, MessageError messageError) {
        fieldExtractor.extractField(request, DAY_OF_EXECUTION_FIELD_NAME, messageError)
            .ifPresent(day -> validateDayOfExecutionValue(day, messageError));
    }

    public void validateMonthsOfExecution(HttpServletRequest request, MessageError messageError) {
       List<String> values = fieldExtractor.extractList(request, MONTHS_OF_EXECUTION_FIELD_NAME, messageError);
        validateMonthsOfExecutionValues(values, messageError);
    }

    public MessageError validateRawDataDates(HttpServletRequest request, Set<Xs2aRequestBodyDateField> fields, MessageError messageError) {
        for (Xs2aRequestBodyDateField field : fields) {
            fieldExtractor.extractField(request, field.getFieldName(), messageError)
                .ifPresent(date -> convert(field.getFieldName(), date, field.getFormatter(), messageError));
        }

        return messageError;
    }

    private void convert(String key, String value, Xs2aBodyDateFormatter formatter, MessageError messageError) {
        try {
            localDateConverter.convert(value, formatter.getFormatter());
        } catch (DateTimeParseException ex) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, key, formatter.name(), formatter.getPattern()));
        }
    }

    private void validateDayOfExecutionValue(String value, MessageError messageError) {
        if (!isNumberADayOfMonth(value)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_INVALID_DAY_OF_EXECUTION));
        }
    }

    private void validateMonthsOfExecutionValues(List<String> values, MessageError messageError) {
        Set<String> distinctValues = new HashSet<>(values);
        if (values.isEmpty()
                || values.size() > 11
                || values.size() > distinctValues.size()) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_INVALID_SIZE_MONTHS_OF_EXECUTION));
        }
        boolean isAllValuesCorrect = values.stream()
                                .map(this::isNumberAMonthOfYear)
                                .filter(bool -> !bool)
                                .findAny()
                                .orElse(true);

        if (!isAllValuesCorrect) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_INVALID_MONTHS_OF_EXECUTION));
        }
    }

    private boolean isNumberADayOfMonth(@NotNull String value) {
        return value.matches(DAY_OF_MONTH_REGEX);
    }

    private boolean isNumberAMonthOfYear(@NotNull String value) {
        return value.matches(MONTH_OF_YEAR_REGEX);
    }
}
