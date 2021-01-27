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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.validator.TppNotificationDataValidator;
import de.adorsys.psd2.xs2a.service.validator.TppUriHeaderValidator;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.CommonConsentObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.*;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentAuthorisationObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.UpdateConsentPsuDataRequestObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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
    private final TppUriHeaderValidator tppUriHeaderValidator;
    private final TppNotificationDataValidator tppNotificationDataValidator;

    public ValidationResult validateConsentOnCreate(CreateConsentReq request, PsuIdData psuIdData) {
        return createConsentRequestValidator.validate(new CreateConsentRequestObject(request, psuIdData));
    }

    public ValidationResult validateConsentOnGettingStatusById(AisConsent consent) {
        return getAccountConsentsStatusByIdValidator.validate(new CommonConsentObject(consent));
    }

    public ValidationResult validateConsentOnDelete(AisConsent consent) {
        return deleteAccountConsentsByIdValidator.validate(new CommonConsentObject(consent));
    }

    public ValidationResult validateConsentOnGettingById(AisConsent consent) {
        return getAccountConsentByIdValidator.validate(new CommonConsentObject(consent));
    }

    public ValidationResult validateConsentAuthorisationOnCreate(CreateConsentAuthorisationObject createConsentAuthorisationObject) {
        return createConsentAuthorisationValidator.validate(createConsentAuthorisationObject);
    }

    public ValidationResult validateConsentPsuDataOnUpdate(AisConsent consent, UpdateConsentPsuDataReq request) {
        return updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(consent, request));
    }

    public ValidationResult validateConsentAuthorisationOnGettingById(AisConsent consent) {
        return getConsentAuthorisationsValidator.validate(new CommonConsentObject(consent));
    }

    public ValidationResult validateConsentAuthorisationScaStatus(AisConsent consent, String authorisationId) {
        return getConsentAuthorisationScaStatusValidator.validate(new GetConsentAuthorisationScaStatusPO(consent, authorisationId));
    }

    public Set<TppMessageInformation> buildWarningMessages(CreateConsentReq request) {
        Set<TppMessageInformation> warnings = new HashSet<>();

        warnings.addAll(tppUriHeaderValidator.buildWarningMessages(request.getTppRedirectUri()));
        warnings.addAll(tppNotificationDataValidator.buildWarningMessages(request.getTppNotificationData()));

        return warnings;
    }
}
