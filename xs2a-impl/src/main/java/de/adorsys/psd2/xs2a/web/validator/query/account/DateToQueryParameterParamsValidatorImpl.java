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
import de.adorsys.psd2.xs2a.web.validator.query.AbstractQueryParameterValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_DATE_PERIOD_INVALID;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_FIELD;

@Component
public class DateToQueryParameterParamsValidatorImpl extends AbstractQueryParameterValidatorImpl
    implements TransactionListQueryParamsValidator {

    private static final String DATE_FROM_PARAMETER_NAME = "dateFrom";
    private static final String DATE_TO_PARAMETER_NAME = "dateTo";

    public DateToQueryParameterParamsValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getQueryParameterName() {
        return DATE_TO_PARAMETER_NAME;
    }

    @Override
    public MessageError validate(Map<String, List<String>> queryParameterMap, MessageError messageError) {
        String dateTo = getQueryParameterValue(queryParameterMap, getQueryParameterName());
        if (dateTo != null && !isDateParamValid(dateTo)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_INVALID_FIELD, getQueryParameterName()));
            return messageError;
        }

        String dateFrom = getQueryParameterValue(queryParameterMap, DATE_FROM_PARAMETER_NAME);
        if (StringUtils.isNoneEmpty(dateTo, dateFrom)
                && isDateParamValid(dateFrom)
                && LocalDate.parse(dateTo).isBefore(LocalDate.parse(dateFrom))) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_DATE_PERIOD_INVALID,
                                                                                           DATE_TO_PARAMETER_NAME, DATE_FROM_PARAMETER_NAME));
        }

        return messageError;
    }
}
