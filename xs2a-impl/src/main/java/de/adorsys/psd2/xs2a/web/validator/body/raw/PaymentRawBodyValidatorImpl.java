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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.converter.LocalDateConverter;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateFields.PAYMENT_DATE_FIELDS;

@Component
public class PaymentRawBodyValidatorImpl extends AbstractRawBodyValidatorImpl implements PaymentRawBodyValidator {

    private static final String DAY_OF_EXECUTION_FIELD_NAME = "dayOfExecution";
    private static final String DAY_OF_MONTH_REGEX = "(0?[1-9]|[12]\\d|3[01])";
    private static final String DAY_OF_EXECUTION_WRONG_VALUE_ERROR = "Value 'dayOfExecution' should be a number of day in month";

    protected PaymentRawBodyValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper, JsonConverter jsonConverter, LocalDateConverter localDateConverter) {
        super(errorBuildingService, objectMapper, jsonConverter, localDateConverter);
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {
        String dayOfExecution = extractField(request, DAY_OF_EXECUTION_FIELD_NAME, messageError);
        validateDayOfExecutionValue(dayOfExecution, messageError);

        validateRawDataDates(request, PAYMENT_DATE_FIELDS.getDateFields(), messageError);
    }

    private void validateDayOfExecutionValue(String value, MessageError messageError) {
        if (value == null) {
            return;
        }

        if (!isNumberADayOfMonth(value)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, DAY_OF_EXECUTION_WRONG_VALUE_ERROR));
        }
    }

    private boolean isNumberADayOfMonth(@NotNull String value) {
        return value.matches(DAY_OF_MONTH_REGEX);
    }
}
