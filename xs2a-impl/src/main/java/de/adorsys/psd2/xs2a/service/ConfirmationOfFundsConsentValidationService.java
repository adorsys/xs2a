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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.service.validator.piis.*;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentAuthorisationObject;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.UpdatePiisConsentPsuDataRequestObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmationOfFundsConsentValidationService {
    private final DeleteConfirmationOfFundsConsentByIdValidator deleteConfirmationOfFundsConsentByIdValidator;
    private final CreatePiisConsentAuthorisationValidator createPiisConsentAuthorisationValidator;
    private final UpdatePiisConsentPsuDataValidator updatePiisConsentPsuDataValidator;
    private final GetConfirmationOfFundsConsentAuthorisationsValidator getCofConsentAuthorisationsValidator;
    private final GetConfirmationOfFundsConsentAuthorisationScaStatusValidator getCofConsentScaStatusValidator;

    public ValidationResult validateConsentOnDelete(PiisConsent consent) {
        return deleteConfirmationOfFundsConsentByIdValidator.validate(new CommonConfirmationOfFundsConsentObject(consent));
    }

    public ValidationResult validateConsentAuthorisationOnCreate(CreatePiisConsentAuthorisationObject createPiisConsentAuthorisationObject) {
        return createPiisConsentAuthorisationValidator.validate(createPiisConsentAuthorisationObject);
    }

    public ValidationResult validateConsentPsuDataOnUpdate(PiisConsent consent, ConsentAuthorisationsParameters request) {
        return updatePiisConsentPsuDataValidator.validate(new UpdatePiisConsentPsuDataRequestObject(consent, request));
    }

    public ValidationResult validateConsentAuthorisationOnGettingById(PiisConsent consent) {
        return getCofConsentAuthorisationsValidator.validate(new CommonConfirmationOfFundsConsentObject(consent));
    }

    public ValidationResult validateConsentAuthorisationScaStatus(PiisConsent consent, String authorisationId) {
        return getCofConsentScaStatusValidator.validate(new GetConfirmationOfFundsConsentAuthorisationScaStatusPO(consent, authorisationId));
    }
}
