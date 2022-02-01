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
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
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
public class CreateConsentAuthorisationValidator extends AbstractAisTppValidator<CreateConsentAuthorisationObject> {

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

        if (aisConsent.isSigningBasketAuthorised()) {
            return ValidationResult.invalid(AIS_400, STATUS_INVALID);
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
