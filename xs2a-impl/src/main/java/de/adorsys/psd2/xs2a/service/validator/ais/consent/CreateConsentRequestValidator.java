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

import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.SupportedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentRequestObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.EMBEDDED;

/**
 * Validator to be used for validating create consent request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class CreateConsentRequestValidator implements BusinessValidator<CreateConsentRequestObject> {

    private final AspspProfileServiceWrapper aspspProfileService;
    private final ScaApproachResolver scaApproachResolver;
    private final PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    private final SupportedAccountReferenceValidator supportedAccountReferenceValidator;

    /**
     * Validates Create consent request according to:
     * <ul>
     * <li>the presence of PSU Data in the request if it's mandated by the profile</li>
     * <li>support of account reference types</li>
     * <li>support of global consent for All Psd2</li>
     * <li>support of bank offered consent</li>
     * <li>support of available account access</li>
     * <li>support of combined service indicator</li>
     * </ul>
     * If there are new consent requirements, this method has to be updated.
     *
     * @param requestObject create consent request object
     * @return ValidationResult instance, that contains boolean isValid, that shows if request is valid
     * and MessageError for invalid case
     */
    @NotNull
    @Override
    public ValidationResult validate(@NotNull CreateConsentRequestObject requestObject) {
        ValidationResult psuDataValidationResult = psuDataInInitialRequestValidator.validate(requestObject.getPsuIdData());
        if (psuDataValidationResult.isNotValid()) {
            return psuDataValidationResult;
        }

        CreateConsentReq request = requestObject.getCreateConsentReq();

        ValidationResult supportedAccountReferenceValidationResult = supportedAccountReferenceValidator.validate(request.getAccountReferences());
        if (supportedAccountReferenceValidationResult.isNotValid()) {
            return supportedAccountReferenceValidationResult;
        }

        if (isNotSupportedGlobalConsentForAllPsd2(request)) {
            return ValidationResult.invalid(ErrorType.AIS_405, SERVICE_INVALID_400_FOR_GLOBAL_CONSENT);
        }
        if (isNotSupportedBankOfferedConsent(request)) {
            return ValidationResult.invalid(ErrorType.AIS_405, SERVICE_INVALID_400);
        }
        if (isNotSupportedAvailableAccounts(request)) {
            return ValidationResult.invalid(ErrorType.AIS_405, SERVICE_INVALID_400);
        }
        if (isNotSupportedCombinedServiceIndicator(request)) {
            return ValidationResult.invalid(ErrorType.AIS_400, SESSIONS_NOT_SUPPORTED);
        }

        return ValidationResult.valid();
    }

    private boolean isNotSupportedGlobalConsentForAllPsd2(CreateConsentReq request) {
        return isConsentGlobal(request)
                   && !aspspProfileService.isGlobalConsentSupported();
    }

    private boolean isNotSupportedBankOfferedConsent(CreateConsentReq request) {
        if (isNotEmptyAccess(request.getAccess())) {
            return false;
        }

        if (scaApproachResolver.resolveScaApproach() == EMBEDDED) {
            return true;
        }

        return !aspspProfileService.isBankOfferedConsentSupported();
    }

    private boolean isConsentGlobal(CreateConsentReq request) {
        return isNotEmptyAccess(request.getAccess())
                   && request.getAccess().getAllPsd2() == ALL_ACCOUNTS;
    }

    private boolean isNotEmptyAccess(Xs2aAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(Xs2aAccountAccess::isNotEmpty)
                   .orElse(false);
    }

    private boolean isNotSupportedAvailableAccounts(CreateConsentReq request) {
        if (Objects.isNull(request.getAccess().getAvailableAccounts())) {
            return false;
        }

        return !aspspProfileService.isAvailableAccountsConsentSupported();
    }

    private boolean isNotSupportedCombinedServiceIndicator(CreateConsentReq request) {
        return request.isCombinedServiceIndicator()
                   && !aspspProfileService.isAisPisSessionsSupported();
    }
}
