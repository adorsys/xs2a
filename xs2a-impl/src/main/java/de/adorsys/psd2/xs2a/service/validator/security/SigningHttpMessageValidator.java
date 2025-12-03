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

package de.adorsys.psd2.xs2a.service.validator.security;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.security.SigningHttpMessage;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

//TODO: Implement actual validation logic for signing HTTP message Service
/**
 * Validator for SigningHttpMessage according to business rules.
 * Validates digest and X-JWS-Signature fields for HTTP message signing.
 */
@Component
@RequiredArgsConstructor
public class SigningHttpMessageValidator implements BusinessValidator<SigningHttpMessage> {

    /**
     * Validates SigningHttpMessage according to business rules.
     * Checks if digest and X-JWS-Signature are properly formatted and consistent.
     *
     * @param signingHttpMessage the SigningHttpMessage object to be validated
     * @return valid result if the message is valid, invalid result with appropriate error otherwise
     */
    @Override
    public @NotNull ValidationResult validate(@NotNull SigningHttpMessage signingHttpMessage) {
        // For now, we accept any SigningHttpMessage as valid
        // Future implementations may add specific validation rules for:
        // - Digest format validation
        // - X-JWS-Signature format validation
        // - Signature verification
        return ValidationResult.valid();
    }

    /**
     * Builds warning messages for SigningHttpMessage if there are inconsistencies.
     *
     * @param signingHttpMessage the SigningHttpMessage object to check
     * @return empty set if no warnings, set of warning messages otherwise
     */
    @Override
    public @NotNull Set<TppMessageInformation> buildWarningMessages(@NotNull SigningHttpMessage signingHttpMessage) {
        // No warnings for now
        // Future implementations may add warnings for:
        // - Missing optional signature components
        // - Weak digest algorithms
        // - Deprecated signature formats
        return Collections.emptySet();
    }
}
