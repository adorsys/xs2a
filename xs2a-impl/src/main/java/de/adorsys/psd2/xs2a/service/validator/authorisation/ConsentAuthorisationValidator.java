/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.validator.authorisation;

import de.adorsys.psd2.core.data.ConsentAutorizable;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;

@Slf4j
public abstract class ConsentAuthorisationValidator {

    @NotNull
    public ValidationResult validate(@NotNull String authorisationId, @NotNull ConsentAutorizable consent) {
        Optional<ConsentAuthorization> authorisationOptional = consent.findAuthorisationInConsent(authorisationId);
        if (authorisationOptional.isEmpty()) {
            log.info("Consent ID: [{}], Authorisation ID: [{}]. Authorisation validation has failed: couldn't find authorisation with given authorisationId for consent",
                     consent.getId(), authorisationId);
            return ValidationResult.invalid(getErrorType(), RESOURCE_UNKNOWN_403);
        }

        return ValidationResult.valid();
    }

    protected abstract ErrorType getErrorType();
}

