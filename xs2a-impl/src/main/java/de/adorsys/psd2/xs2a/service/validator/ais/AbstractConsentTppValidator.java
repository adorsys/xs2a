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

package de.adorsys.psd2.xs2a.service.validator.ais;

import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.TppInfoProvider;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoValidator;
import org.jetbrains.annotations.NotNull;

/**
 * Common validator for validating TPP in consents and executing request-specific business validation afterwards.
 * Should be used for all consent-related requests after consent creation.
 *
 * @param <T> type of object to be checked
 */
public abstract class AbstractConsentTppValidator<T extends TppInfoProvider> implements BusinessValidator<T> {
    @NotNull
    @Override
    public ValidationResult validate(@NotNull T object) {
        TppInfo tppInfoInConsent = object.getTppInfo();
        ValidationResult tppValidationResult = getTppInfoValidator().validateTpp(tppInfoInConsent);
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        return executeBusinessValidation(object);
    }

    /**
     * Executes request-specific business validation
     *
     * @param consentObject consent object to be validated
     * @return valid result if the object is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    protected abstract ValidationResult executeBusinessValidation(T consentObject);

    /**
     * Returns appropriate TPP info validator for current request
     *
     * @return TPP info validator
     */
    @NotNull
    protected abstract TppInfoValidator getTppInfoValidator();
}
