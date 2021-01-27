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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
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

    public ValidationResult validateConsentPsuDataOnUpdate(PiisConsent consent, UpdateConsentPsuDataReq request) {
        return updatePiisConsentPsuDataValidator.validate(new UpdatePiisConsentPsuDataRequestObject(consent, request));
    }

    public ValidationResult validateConsentAuthorisationOnGettingById(PiisConsent consent) {
        return getCofConsentAuthorisationsValidator.validate(new CommonConfirmationOfFundsConsentObject(consent));
    }

    public ValidationResult validateConsentAuthorisationScaStatus(PiisConsent consent, String authorisationId) {
        return getCofConsentScaStatusValidator.validate(new GetConfirmationOfFundsConsentAuthorisationScaStatusPO(consent, authorisationId));
    }
}
