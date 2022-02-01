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
