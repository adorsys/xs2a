/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_FIELD;

/**
 * Validator for Accept header in VOP requests.
 * According to Berlin Group PSD2 API specification, Accept header is optional for VOP endpoints.
 * If provided, it should accept "application/json" or wildcard.
 */
@Component
public class VopAcceptHeaderValidatorImpl extends AbstractHeaderValidatorImpl implements VopHeaderValidator {

    private static final String APPLICATION_JSON = "application/json";
    private static final String WILDCARD = "*/*";

    @Autowired
    public VopAcceptHeaderValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return HttpHeaders.ACCEPT;
    }

    @Override
    protected ValidationResult validate(Map<String, String> headers) {
        // Accept header is optional - only validate if present
        String header = headers.get(getHeaderName());
        if (header == null || header.trim().isEmpty()) {
            return ValidationResult.valid();
        }
        return checkHeaderContent(headers);
    }

    @Override
    protected ValidationResult checkHeaderContent(Map<String, String> headers) {
        String header = headers.get(getHeaderName());
        // Accept both "application/json" and "*/*" (wildcard)
        if (header != null && !header.toLowerCase().contains(APPLICATION_JSON) && !header.contains(WILDCARD)) {
            return ValidationResult.invalid(
                errorBuildingService.buildErrorType(), TppMessageInformation.of(FORMAT_ERROR_INVALID_FIELD, getHeaderName()));
        }

        return ValidationResult.valid();
    }
}
