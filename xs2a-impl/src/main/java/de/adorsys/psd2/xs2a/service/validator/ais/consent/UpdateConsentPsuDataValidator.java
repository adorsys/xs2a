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

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.service.validator.AisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.UpdateConsentPsuDataRequestObject;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;

/**
 * Validator to be used for validating update consent psu data request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class UpdateConsentPsuDataValidator extends AbstractAisTppValidator<UpdateConsentPsuDataRequestObject> {
    private final AisAuthorisationValidator aisAuthorisationValidator;
    private final AisAuthorisationStatusValidator aisAuthorisationStatusValidator;
    private final AisPsuDataUpdateAuthorisationCheckerValidator aisPsuDataUpdateAuthorisationCheckerValidator;
    private final AuthorisationStageCheckValidator authorisationStageCheckValidator;

    /**
     * Validates update consent psu data request
     *
     * @param requestObject consent information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(UpdateConsentPsuDataRequestObject requestObject) {
        AisConsent consent = requestObject.getAisConsent();
        ConsentAuthorisationsParameters updatePsuData = requestObject.getUpdateRequest();
        String authorisationId = updatePsuData.getAuthorizationId();

        ValidationResult authorisationValidationResult = aisAuthorisationValidator.validate(authorisationId, consent);
        if (authorisationValidationResult.isNotValid()) {
            return authorisationValidationResult;
        }

        Optional<ConsentAuthorization> authorisationOptional = consent.findAuthorisationInConsent(authorisationId);

        if (authorisationOptional.isEmpty()) {
            return ValidationResult.invalid(ErrorType.AIS_403, RESOURCE_UNKNOWN_403);
        }

        ConsentAuthorization authorisation = authorisationOptional.get();

        ValidationResult validationResult = aisPsuDataUpdateAuthorisationCheckerValidator.validate(updatePsuData.getPsuData(), authorisation.getPsuIdData());

        if (validationResult.isNotValid()) {
            return validationResult;
        }

        ValidationResult authorisationStatusValidationResult = aisAuthorisationStatusValidator.validate(authorisation.getScaStatus(), StringUtils.isNotBlank(updatePsuData.getConfirmationCode()));
        if (authorisationStatusValidationResult.isNotValid()) {
            return authorisationStatusValidationResult;
        }

        ValidationResult authorisationStageCheckValidatorResult = authorisationStageCheckValidator.validate(updatePsuData, authorisation.getScaStatus(), AuthorisationServiceType.AIS);
        if (authorisationStageCheckValidatorResult.isNotValid()) {
            return authorisationStageCheckValidatorResult;
        }

        return ValidationResult.valid();
    }
}
