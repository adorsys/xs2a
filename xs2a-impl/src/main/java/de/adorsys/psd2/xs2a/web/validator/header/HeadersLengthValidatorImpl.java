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

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.HEADERS_MAX_LENGTHS;


@Component
public class HeadersLengthValidatorImpl extends AbstractHeaderValidatorImpl
    implements ConsentHeaderValidator, PaymentHeaderValidator {

    private static final String HEADER_LENGTH_ERROR_TEXT = "Header '%s' should not be more than %s symbols";

    @Autowired
    public HeadersLengthValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return null;
    }

    @Override
    public void validate(Map<String, String> inputHeaders, MessageError messageError) {
        for (Map.Entry<String, String> header : inputHeaders.entrySet()) {
            if (isHeaderExceedsLength(header)) {
                String resultingMessage = prepareErrorMessage(header.getKey());
                errorBuildingService.enrichMessageError(messageError, resultingMessage);
            }
        }
    }

    private boolean isHeaderExceedsLength(Map.Entry<String, String> header) {
        String headerName = header.getKey();
        String headerValue = header.getValue();
        if (headerName == null || headerValue == null) {
            return false; // no header - no length check
        }
        String headerNameLowerCase = headerName.toLowerCase();
        Set<String> headersToValidate = HEADERS_MAX_LENGTHS.keySet();
        return headersToValidate.contains(headerNameLowerCase)
                        && headerValue.length() > HEADERS_MAX_LENGTHS.get(headerNameLowerCase);
    }

    private String prepareErrorMessage(String headerNameLowerCase) {
        return String.format(HEADER_LENGTH_ERROR_TEXT, headerNameLowerCase, HEADERS_MAX_LENGTHS.get(headerNameLowerCase));
    }
}
