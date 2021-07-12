/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.service.validator.PiisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.AbstractAisTppValidator;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.UpdatePiisConsentPsuDataRequestObject;
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
public class UpdatePiisConsentPsuDataValidator extends AbstractAisTppValidator<UpdatePiisConsentPsuDataRequestObject> {
    private final PiisAuthorisationValidator piisAuthorisationValidator;
    private final PiisPsuDataUpdateAuthorisationCheckerValidator piisPsuDataUpdateAuthorisationCheckerValidator;
    private final PiisAuthorisationStatusValidator piisAuthorisationStatusValidator;
    private final AuthorisationStageCheckValidator authorisationStageCheckValidator;

    /**
     * Validates update consent psu data request
     *
     * @param requestObject consent information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(UpdatePiisConsentPsuDataRequestObject requestObject) {
        PiisConsent consent = requestObject.getPiisConsent();
        ConsentAuthorisationsParameters updatePsuData = requestObject.getUpdateRequest();
        String authorisationId = updatePsuData.getAuthorizationId();

        ValidationResult authorisationValidationResult = piisAuthorisationValidator.validate(authorisationId, consent);
        if (authorisationValidationResult.isNotValid()) {
            return authorisationValidationResult;
        }

        Optional<ConsentAuthorization> authorisationOptional = consent.findAuthorisationInConsent(authorisationId);

        if (authorisationOptional.isEmpty()) {
            return ValidationResult.invalid(ErrorType.PIIS_403, RESOURCE_UNKNOWN_403);
        }

        ConsentAuthorization authorisation = authorisationOptional.get();

        ValidationResult validationResult = piisPsuDataUpdateAuthorisationCheckerValidator.validate(updatePsuData.getPsuData(), authorisation.getPsuIdData());

        if (validationResult.isNotValid()) {
            return validationResult;
        }

        ValidationResult authorisationStatusValidationResult = piisAuthorisationStatusValidator.validate(authorisation.getScaStatus(), StringUtils.isNotBlank(updatePsuData.getConfirmationCode()));
        if (authorisationStatusValidationResult.isNotValid()) {
            return authorisationStatusValidationResult;
        }

        ValidationResult authorisationStageCheckValidatorResult = authorisationStageCheckValidator.validate(updatePsuData, authorisation.getScaStatus(), AuthorisationServiceType.PIIS);
        if (authorisationStageCheckValidatorResult.isNotValid()) {
            return authorisationStageCheckValidatorResult;
        }

        return ValidationResult.valid();
    }
}
