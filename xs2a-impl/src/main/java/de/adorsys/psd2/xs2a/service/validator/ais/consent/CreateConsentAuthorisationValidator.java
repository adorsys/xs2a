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

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentAuthorisationObject;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationPsuDataChecker;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStatusChecker;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

/**
 * Validator to be used for validating create consent authorisation request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class CreateConsentAuthorisationValidator extends AbstractConsentTppValidator<CreateConsentAuthorisationObject> {

    private final AuthorisationPsuDataChecker authorisationPsuDataChecker;
    private final AuthorisationStatusChecker authorisationStatusChecker;

    /**
     * Validates create consent authorisation request
     *
     * @param createConsentAuthorisationObject create consent authorisation information object
     * @return valid result if the create authorisation flow is correct, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(CreateConsentAuthorisationObject createConsentAuthorisationObject) {

        AisConsent aisConsent = createConsentAuthorisationObject.getAisConsent();
        if (aisConsent.isSigningBasketBlocked()) {
            return ValidationResult.invalid(AIS_400, RESOURCE_BLOCKED_SB);
        }

        List<PsuIdData> psuDataFromDb = aisConsent.getPsuIdDataList();
        PsuIdData psuDataFromRequest = createConsentAuthorisationObject.getPsuIdDataFromRequest();
        if (authorisationPsuDataChecker.isPsuDataWrong(
            aisConsent.isMultilevelScaRequired(),
            psuDataFromDb,
            psuDataFromRequest)) {

            return ValidationResult.invalid(AIS_401, PSU_CREDENTIALS_INVALID);
        }

        // If the authorisation for this consent ID and for this PSU ID has status FINALISED or EXEMPTED - return error.
        List<ConsentAuthorization> accountConsentAuthorisations = aisConsent.getAuthorisations();
        List<Authorisation> authorisations = accountConsentAuthorisations.stream()
                                                 .map(auth -> new Authorisation(auth.getId(),
                                                                                auth.getPsuIdData(),
                                                                                auth.getConsentId(),
                                                                                AuthorisationType.CONSENT,
                                                                                auth.getScaStatus()))
                                                 .collect(Collectors.toList());
        boolean isFinalised = authorisationStatusChecker.isFinalised(psuDataFromRequest, authorisations, AuthorisationType.CONSENT);

        if (isFinalised) {
            return ValidationResult.invalid(AIS_409, STATUS_INVALID);
        }

        return ValidationResult.valid();
    }

}
