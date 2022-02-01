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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.OauthPiisConsentValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Validator to be used for validating get consent authorisation sca status request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class GetConfirmationOfFundsConsentAuthorisationScaStatusValidator extends AbstractConfirmationOfFundsConsentTppValidator<GetConfirmationOfFundsConsentAuthorisationScaStatusPO> {
    private final ConfirmationOfFundsAuthorisationValidator confirmationOfFundsAuthorisationValidator;
    private final OauthPiisConsentValidator oauthPiisConsentValidator;

    /**
     * Validates get consent authorisation sca status request
     *
     * @param consentObject consent information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(GetConfirmationOfFundsConsentAuthorisationScaStatusPO consentObject) {
        PiisConsent piisConsent = consentObject.getPiisConsent();
        String authorisationId = consentObject.getAuthorisationId();

        ValidationResult authorisationValidationResult = confirmationOfFundsAuthorisationValidator.validate(authorisationId, piisConsent);
        if (authorisationValidationResult.isNotValid()) {
            return authorisationValidationResult;
        }

        ValidationResult authAuthorisationValidationResult = oauthPiisConsentValidator.validate(piisConsent);
        if (authAuthorisationValidationResult.isNotValid()) {
            return authAuthorisationValidationResult;
        }

        return ValidationResult.valid();
    }
}
