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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.CommonConsentObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.*;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentAuthorisationObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.UpdateConsentPsuDataRequestObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsentValidationService {

    private final CreateConsentRequestValidator createConsentRequestValidator;
    private final GetAccountConsentsStatusByIdValidator getAccountConsentsStatusByIdValidator;
    private final GetAccountConsentByIdValidator getAccountConsentByIdValidator;
    private final DeleteAccountConsentsByIdValidator deleteAccountConsentsByIdValidator;
    private final CreateConsentAuthorisationValidator createConsentAuthorisationValidator;
    private final UpdateConsentPsuDataValidator updateConsentPsuDataValidator;
    private final GetConsentAuthorisationsValidator getConsentAuthorisationsValidator;
    private final GetConsentAuthorisationScaStatusValidator getConsentAuthorisationScaStatusValidator;

    public ValidationResult validateConsentOnCreate(CreateConsentReq request, PsuIdData psuIdData) {
        return createConsentRequestValidator.validate(new CreateConsentRequestObject(request, psuIdData));
    }

    public ValidationResult validateConsentOnGettingStatusById(AccountConsent consent) {
        return getAccountConsentsStatusByIdValidator.validate(new CommonConsentObject(consent));
    }

    public ValidationResult validateConsentOnDelete(AccountConsent consent) {
        return deleteAccountConsentsByIdValidator.validate(new CommonConsentObject(consent));
    }

    public ValidationResult validateConsentOnGettingById(AccountConsent consent) {
        return getAccountConsentByIdValidator.validate(new CommonConsentObject(consent));
    }

    public ValidationResult validateConsentAuthorisationOnCreate(CreateConsentAuthorisationObject createConsentAuthorisationObject) {
        return createConsentAuthorisationValidator.validate(createConsentAuthorisationObject);
    }

    public ValidationResult validateConsentPsuDataOnUpdate(AccountConsent consent, UpdateConsentPsuDataReq request) {
        return updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(consent, request));
    }

    public ValidationResult validateConsentAuthorisationOnGettingById(AccountConsent consent) {
        return getConsentAuthorisationsValidator.validate(new CommonConsentObject(consent));
    }

    public ValidationResult validateConsentAuthorisationScaStatus(AccountConsent consent, String authorisationId) {
        return getConsentAuthorisationScaStatusValidator.validate(new GetConsentAuthorisationScaStatusPO(consent, authorisationId));
    }
}
