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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.validator.TppNotificationDataValidator;
import de.adorsys.psd2.xs2a.service.validator.TppUriHeaderValidator;
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

    public ValidationResult validateConsentPsuDataOnUpdate(AisConsent consent, ConsentAuthorisationsParameters request) {
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
