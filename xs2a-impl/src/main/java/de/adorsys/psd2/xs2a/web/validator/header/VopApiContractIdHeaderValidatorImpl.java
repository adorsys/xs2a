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
import org.springframework.stereotype.Component;

import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_WRONG_HEADER;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.API_CONTRACT_ID;

/**
 * Validator for API-Contract-ID header in VOP requests.
 * According to Berlin Group PSD2 API specification, API-Contract-ID header is optional for VOP endpoints.
 * If provided, it should contain the contract identifier for API access.
 */
@Component
public class VopApiContractIdHeaderValidatorImpl extends AbstractHeaderValidatorImpl implements VopHeaderValidator {

    @Autowired
    public VopApiContractIdHeaderValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return API_CONTRACT_ID;
    }

    @Override
    protected ValidationResult validate(Map<String, String> headers) {
        // API-Contract-ID header is optional - only validate if present
        String header = headers.get(getHeaderName());
        if (header == null || header.trim().isEmpty()) {
            return ValidationResult.valid();
        }
        return checkHeaderContent(headers);
    }

    @Override
    protected ValidationResult checkHeaderContent(Map<String, String> headers) {
        String header = headers.get(getHeaderName());
        
        if (isNonValid(header)) {
            return ValidationResult.invalid(
                errorBuildingService.buildErrorType(), TppMessageInformation.of(FORMAT_ERROR_WRONG_HEADER, getHeaderName()));
        }

        return ValidationResult.valid();
    }

    private boolean isNonValid(String contractId) {
        if (contractId == null || contractId.trim().isEmpty()) {
            return true;
        }

        // Basic validation - should be a non-empty string with reasonable length
        // Contract IDs are typically alphanumeric strings
        return contractId.length() < 3 || contractId.length() > 100 || !contractId.matches("^[a-zA-Z0-9_-]+$");
    }
}