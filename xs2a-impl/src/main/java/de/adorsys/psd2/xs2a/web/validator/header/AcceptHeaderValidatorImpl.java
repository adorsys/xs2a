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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.account.TransactionListHeaderValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_BLANK_HEADER;
import static org.springframework.http.HttpHeaders.ACCEPT;

/**
 * Validator to be used to validate 'Accept' header in all REST calls.
 */
@Component
public class AcceptHeaderValidatorImpl extends AbstractHeaderValidatorImpl
    implements TransactionListHeaderValidator {

    @Autowired
    public AcceptHeaderValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return ACCEPT;
    }

    @Override
    public MessageError validate(Map<String, String> headers, MessageError messageError) {
        String header = headers.get(getHeaderName());
        if (Objects.nonNull(header) && StringUtils.isBlank(header)) {
            errorBuildingService.enrichMessageError(messageError,
                                                    TppMessageInformation.of(FORMAT_ERROR_BLANK_HEADER, getHeaderName()));
        }

        return messageError;
    }
}
