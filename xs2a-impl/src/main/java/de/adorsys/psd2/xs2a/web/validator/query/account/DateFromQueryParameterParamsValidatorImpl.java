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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_ABSENT_PARAMETER;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_FIELD;

@Component
public class DateFromQueryParameterParamsValidatorImpl extends AbstractQueryParameterValidatorImpl
    implements TransactionListQueryParamsValidator {
    private static final String DATE_FROM_PARAMETER_NAME = "dateFrom";
    private static final String ENTRY_REFERENCE_FROM_PARAMETER_NAME = "entryReferenceFrom";
    private static final String DELTA_LIST_PARAMETER_NAME = "deltaList";
    private static final String BOOKING_STATUS_PARAMETER_NAME = "bookingStatus";

    public DateFromQueryParameterParamsValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getQueryParameterName() {
        return DATE_FROM_PARAMETER_NAME;
    }

    @Override
    public MessageError validate(Map<String, List<String>> queryParameterMap, MessageError messageError) {
        String dateFrom = getQueryParameterValue(queryParameterMap, getQueryParameterName());

        if (dateFrom != null && !isDateParamValid(dateFrom)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_INVALID_FIELD, getQueryParameterName()));
        }

        boolean isBookingStatusInformation = queryParameterMap.get(BOOKING_STATUS_PARAMETER_NAME) != null
                                                 && queryParameterMap.get(BOOKING_STATUS_PARAMETER_NAME).size() == 1
                                                 && queryParameterMap.get(BOOKING_STATUS_PARAMETER_NAME).get(0).equalsIgnoreCase("information");

        if (dateFrom == null && !isDeltaAccess(queryParameterMap) && !isBookingStatusInformation) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_ABSENT_PARAMETER, getQueryParameterName()));
        }

        return messageError;
    }

    private boolean isDeltaAccess(Map<String, List<String>> queryParameterMap) {
        String entryReferenceFrom = getQueryParameterValue(queryParameterMap, ENTRY_REFERENCE_FROM_PARAMETER_NAME);
        String deltaList = getQueryParameterValue(queryParameterMap, DELTA_LIST_PARAMETER_NAME);
        return entryReferenceFrom != null || deltaList != null;
    }
}
