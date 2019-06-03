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

package de.adorsys.psd2.xs2a.web.validator.query.account;

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.query.AbstractQueryParameterValidatorImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class BookingStatusQueryParameterParamsValidatorImpl extends AbstractQueryParameterValidatorImpl
    implements TransactionListQueryParamsValidator {
    private static final String BOOKING_STATUS_PARAMETER_NAME = "bookingStatus";
    private static final String ERROR_TEXT_INVALID_VALUE = "Query parameter '%s' has invalid value";

    public BookingStatusQueryParameterParamsValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getQueryParameterName() {
        return BOOKING_STATUS_PARAMETER_NAME;
    }

    @Override
    public void validate(Map<String, List<String>> queryParameterMap, MessageError messageError) {
        ValidationResult presenceValidationResult = validateMandatoryParameterPresence(queryParameterMap);
        if (presenceValidationResult.isNotValid()) {
            errorBuildingService.enrichMessageError(messageError, presenceValidationResult.getMessageError());
            return;
        }

        String bookingStatusValue = getQueryParameterValue(queryParameterMap);
        Optional<BookingStatus> bookingStatusOptional = BookingStatus.getByValue(bookingStatusValue);
        if (!bookingStatusOptional.isPresent()) {
            errorBuildingService.enrichMessageError(messageError, String.format(ERROR_TEXT_INVALID_VALUE, getQueryParameterName()));
        }
    }
}
