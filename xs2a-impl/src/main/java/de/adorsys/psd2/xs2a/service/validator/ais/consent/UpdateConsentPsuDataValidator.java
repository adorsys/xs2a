/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.PsuDataUpdateAuthorisationChecker;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.UpdateConsentPsuDataRequestObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_401;

/**
 * Validator to be used for validating update consent psu data request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class UpdateConsentPsuDataValidator extends AbstractConsentTppValidator<UpdateConsentPsuDataRequestObject> {
    private final AisAuthorisationStatusValidator aisAuthorisationStatusValidator;
    private final PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker;

    /**
     * Validates update consent psu data request
     *
     * @param requestObject consent information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(UpdateConsentPsuDataRequestObject requestObject) {
        AccountConsentAuthorization authorisation = requestObject.getAuthorisation();

        if (psuDataUpdateAuthorisationChecker.areBothPsusAbsent(requestObject.getPsuIdData(), authorisation.getPsuIdData())) {
            return ValidationResult.invalid(new MessageError(ErrorType.AIS_400, of(FORMAT_ERROR_NO_PSU)));
        }

        if (!psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(requestObject.getPsuIdData(), authorisation.getPsuIdData())) {
            return ValidationResult.invalid(new MessageError(AIS_401, of(PSU_CREDENTIALS_INVALID)));
        }

        ValidationResult authorisationValidationResult = aisAuthorisationStatusValidator.validate(authorisation.getScaStatus());
        if (authorisationValidationResult.isNotValid()) {
            return authorisationValidationResult;
        }

        return ValidationResult.valid();
    }
}
