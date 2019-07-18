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

package de.adorsys.psd2.xs2a.web.validator.body;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.converter.LocalDateConverter;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aBodyDateFormatter;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateField;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class DateFieldValidator {
    private static final String DAY_OF_EXECUTION_FIELD_NAME = "dayOfExecution";
    private static final String DAY_OF_MONTH_REGEX = "(0?[1-9]|[12]\\d|3[01])";
    private static final String DAY_OF_EXECUTION_WRONG_VALUE_ERROR = "Value 'dayOfExecution' should be a number of day in month";
    private static final String ERROR_TEXT_ISO_DATE_FORMAT = "Wrong format for '%s': value should be %s '%s' format.";
    private static final String BODY_DESERIALIZATION_ERROR = "Cannot deserialize the request body";

    private final ErrorBuildingService errorBuildingService;
    private final JsonConverter jsonConverter;
    private final LocalDateConverter localDateConverter;

    public void validateDateFormat(HttpServletRequest request, EnumSet<Xs2aRequestBodyDateField> fields, MessageError messageError) {
        validateRawDataDates(request, fields, messageError);
    }

    public void validateDayOfExecution(HttpServletRequest request, MessageError messageError) {
        extractField(request, DAY_OF_EXECUTION_FIELD_NAME, messageError)
            .ifPresent(day -> validateDayOfExecutionValue(day, messageError));
    }

    private void validateRawDataDates(HttpServletRequest request, EnumSet<Xs2aRequestBodyDateField> fields, MessageError messageError) {
        for (Xs2aRequestBodyDateField field : fields) {
            extractField(request, field.getFieldName(), messageError)
                .ifPresent(date -> convert(field.getFieldName(), date, field.getFormatter(), messageError));
        }
    }

    private void convert(String key, String value, Xs2aBodyDateFormatter formatter, MessageError messageError) {
        try {
            localDateConverter.convert(value, formatter.getFormatter());
        } catch (DateTimeParseException ex) {
            errorBuildingService.enrichMessageError(messageError, String.format(ERROR_TEXT_ISO_DATE_FORMAT, key, formatter.name(), formatter.getPattern()));
        }
    }

    private Optional<String> extractField(HttpServletRequest request, String fieldName, MessageError messageError) {
        Optional<String> fieldOptional = Optional.empty();
        try {
            // TODO: create common class with Jackson's functionality instead of two: JsonConverter and ObjectMapper.
            //  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/870
            fieldOptional = jsonConverter.toJsonField(request.getInputStream(), fieldName, new TypeReference<String>() {
            });
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, BODY_DESERIALIZATION_ERROR);
        }

        return fieldOptional;
    }

    private void validateDayOfExecutionValue(String value, MessageError messageError) {
        if (!isNumberADayOfMonth(value)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, DAY_OF_EXECUTION_WRONG_VALUE_ERROR));
        }
    }

    private boolean isNumberADayOfMonth(@NotNull String value) {
        return value.matches(DAY_OF_MONTH_REGEX);
    }
}
