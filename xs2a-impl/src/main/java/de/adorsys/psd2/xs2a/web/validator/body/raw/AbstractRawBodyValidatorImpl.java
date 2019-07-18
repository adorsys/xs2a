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

package de.adorsys.psd2.xs2a.web.validator.body.raw;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.converter.LocalDateConverter;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aBodyDateFormatter;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateField;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Class with common functionality (AIS and PIS) for bodies validating.
 */
public class AbstractRawBodyValidatorImpl {

    private static final String ERROR_TEXT_ISO_DATE_FORMAT = "Wrong format for '%s': value should be %s '%s' format.";
    private static final String BODY_DESERIALIZATION_ERROR = "Cannot deserialize the request body";

    protected ErrorBuildingService errorBuildingService;
    protected ObjectMapper objectMapper;
    protected JsonConverter jsonConverter;
    protected LocalDateConverter localDateConverter;


    protected AbstractRawBodyValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper, JsonConverter jsonConverter, LocalDateConverter localDateConverter) {
        this.errorBuildingService = errorBuildingService;
        this.objectMapper = objectMapper;
        this.jsonConverter = jsonConverter;
        this.localDateConverter = localDateConverter;
    }

    protected void validateRawDataDates(HttpServletRequest request, EnumSet<Xs2aRequestBodyDateField> fields, MessageError messageError) {
        for (Xs2aRequestBodyDateField field : fields) {
            Optional.ofNullable(extractField(request, field.getFieldName(), messageError))
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

    protected String extractField(HttpServletRequest request, String fieldName, MessageError messageError) {
        Optional<String> fieldOptional = Optional.empty();
        try {
            // TODO: create common class with Jackson's functionality instead of two: JsonConverter and ObjectMapper.
            //  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/870
            fieldOptional = jsonConverter.toJsonField(request.getInputStream(), fieldName, new TypeReference<String>() {
            });
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, BODY_DESERIALIZATION_ERROR);
        }

        return fieldOptional.orElse(null);
    }
}
