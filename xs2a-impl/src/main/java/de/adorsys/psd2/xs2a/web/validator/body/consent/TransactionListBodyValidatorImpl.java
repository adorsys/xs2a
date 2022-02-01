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

package de.adorsys.psd2.xs2a.web.validator.body.consent;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.FieldLengthValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;


@Component
public class TransactionListBodyValidatorImpl extends AbstractBodyValidatorImpl implements TransactionListBodyValidator {

    static final String BOOKING_STATUS_PARAM = "bookingStatus";

    @Autowired
    public TransactionListBodyValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                            FieldLengthValidator fieldLengthValidator) {
        super(errorBuildingService, xs2aObjectMapper, fieldLengthValidator);
    }

    @Override
    public MessageError validate(HttpServletRequest request, MessageError messageError) {
        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        if (StringUtils.isNotBlank(acceptHeader)) {
            String bookingStatus = request.getParameter(BOOKING_STATUS_PARAM);
            if (BookingStatus.INFORMATION.getValue().equals(bookingStatus) &&
                    !MediaType.APPLICATION_JSON_VALUE.equals(acceptHeader)) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.REQUESTED_FORMATS_INVALID));
            }
        }
        return messageError;
    }
}
