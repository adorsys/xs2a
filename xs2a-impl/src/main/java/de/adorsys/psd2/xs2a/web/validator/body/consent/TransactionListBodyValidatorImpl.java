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
