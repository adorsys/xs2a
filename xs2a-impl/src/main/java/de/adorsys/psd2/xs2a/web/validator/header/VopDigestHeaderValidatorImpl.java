/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_WRONG_HEADER;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.DIGEST;

/**
 * Validator for Digest header in VOP requests.
 * According to Berlin Group PSD2 API specification, Digest header is optional for VOP endpoints.
 * If provided, it should contain a SHA-256 hash of the request body.
 */
@Component
public class VopDigestHeaderValidatorImpl extends AbstractHeaderValidatorImpl implements VopHeaderValidator {

    @Autowired
    public VopDigestHeaderValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return DIGEST;
    }

    @Override
    protected ValidationResult validate(Map<String, String> headers) {
        // Digest header is optional - only validate if present
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

    private boolean isNonValid(String digest) {
        if (digest == null || digest.trim().isEmpty()) {
            return true;
        }

        // Basic format validation for Digest header (SHA-256 hash format)
        // Expected format: "SHA-256=base64encodedhash"
        return !digest.startsWith("SHA-256=") || digest.length() < 44; // Minimum length for base64 encoded SHA-256
    }
}