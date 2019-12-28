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
