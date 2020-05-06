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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_OVERSIZE_HEADER;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.*;


@Component
public class HeadersLengthValidatorImpl extends AbstractHeaderValidatorImpl
    implements ConsentHeaderValidator, PaymentHeaderValidator {

    static final Map<String, Integer> headerMaxLengths;

    static {
        Map<String, Integer> maxLengthsHolder = new HashMap<>();
        maxLengthsHolder.put(PSU_ID, 50);
        maxLengthsHolder.put(PSU_ID_TYPE, 50);
        maxLengthsHolder.put(PSU_CORPORATE_ID, 50);
        maxLengthsHolder.put(PSU_CORPORATE_ID_TYPE, 50);

        // These 3 values are limited by CMS columns length:
        maxLengthsHolder.put(TPP_REDIRECT_URI, 500);
        maxLengthsHolder.put(TPP_NOK_REDIRECT_URI, 500);
        maxLengthsHolder.put(TPP_BRAND_LOGGING_INFORMATION, 255);

        maxLengthsHolder.put(PSU_IP_ADDRESS, 140);

        headerMaxLengths = Collections.unmodifiableMap(maxLengthsHolder);
    }

    @Autowired
    public HeadersLengthValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return null;
    }

    @Override
    public MessageError validate(Map<String, String> inputHeaders, MessageError messageError) {
        for (Map.Entry<String, String> header : inputHeaders.entrySet()) {
            if (isHeaderExceedsLength(header)) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_OVERSIZE_HEADER, header.getKey(), headerMaxLengths.get(header.getKey())));
            }
        }

        return messageError;
    }

    private boolean isHeaderExceedsLength(Map.Entry<String, String> header) {
        String headerName = header.getKey();
        String headerValue = header.getValue();
        if (headerName == null || headerValue == null) {
            return false; // no header - no length check
        }
        String headerNameLowerCase = headerName.toLowerCase();
        Set<String> headersToValidate = headerMaxLengths.keySet();
        return headersToValidate.contains(headerNameLowerCase)
                   && headerValue.length() > headerMaxLengths.get(headerNameLowerCase);
    }
}
