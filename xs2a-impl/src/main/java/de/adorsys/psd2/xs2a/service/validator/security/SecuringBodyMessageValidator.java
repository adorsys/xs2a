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
import de.adorsys.psd2.xs2a.core.security.SecuringBodyMessage;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

//TODO: Implement actual validation logic for securing Body message Service
/**
 * Validator for SecuringBodyMessage according to business rules.
 * Validates body signature profile, body encryption profile, and body encryption list.
 */
@Component
@RequiredArgsConstructor
public class SecuringBodyMessageValidator implements BusinessValidator<SecuringBodyMessage> {

    /**
     * Validates SecuringBodyMessage according to business rules.
     * Checks if body signature profile, encryption profile, and encryption list are properly formatted.
     *
     * @param securingBodyMessage the SecuringBodyMessage object to be validated
     * @return valid result if the message is valid, invalid result with appropriate error otherwise
     */
    @Override
    public @NotNull ValidationResult validate(@NotNull SecuringBodyMessage securingBodyMessage) {
        // For now, we accept any SecuringBodyMessage as valid
        // Future implementations may add specific validation rules for:
        // - Body signature profile format validation
        // - Body encryption profile format validation
        // - Body encryption list format validation
        // - Compatibility checks between encryption and signature profiles
        return ValidationResult.valid();
    }

    /**
     * Builds warning messages for SecuringBodyMessage if there are inconsistencies.
     *
     * @param securingBodyMessage the SecuringBodyMessage object to check
     * @return empty set if no warnings, set of warning messages otherwise
     */
    @Override
    public @NotNull Set<TppMessageInformation> buildWarningMessages(@NotNull SecuringBodyMessage securingBodyMessage) {
        // No warnings for now
        // Future implementations may add warnings for:
        // - Weak encryption algorithms
        // - Deprecated signature profiles
        // - Missing recommended security headers
        return Collections.emptySet();
    }
}
