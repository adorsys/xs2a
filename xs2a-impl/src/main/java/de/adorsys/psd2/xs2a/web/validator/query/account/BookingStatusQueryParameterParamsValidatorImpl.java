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

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.query.AbstractQueryParameterValidatorImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_PARAMETER_VALUE;

@Component
public class BookingStatusQueryParameterParamsValidatorImpl extends AbstractQueryParameterValidatorImpl
    implements TransactionListQueryParamsValidator {
    private static final String BOOKING_STATUS_PARAMETER_NAME = "bookingStatus";

    public BookingStatusQueryParameterParamsValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getQueryParameterName() {
        return BOOKING_STATUS_PARAMETER_NAME;
    }

    @Override
    public MessageError validate(Map<String, List<String>> queryParameterMap, MessageError messageError) {
        ValidationResult presenceValidationResult = validateMandatoryParameterPresence(queryParameterMap);
        if (presenceValidationResult.isNotValid()) {
            errorBuildingService.enrichMessageError(messageError, presenceValidationResult.getMessageError());
            return messageError;
        }

        String bookingStatusValue = getQueryParameterValue(queryParameterMap);
        Optional<BookingStatus> bookingStatusOptional = BookingStatus.getByValue(bookingStatusValue);
        if (bookingStatusOptional.isEmpty()) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_INVALID_PARAMETER_VALUE, getQueryParameterName()));
        }

        return messageError;
    }
}
