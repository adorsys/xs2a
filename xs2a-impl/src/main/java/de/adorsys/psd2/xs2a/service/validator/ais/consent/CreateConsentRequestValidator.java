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

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.SupportedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentRequestObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS;
import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS_WITH_OWNER_NAME;
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
            return ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400_FOR_GLOBAL_CONSENT);
        }
        if (isNotSupportedBankOfferedConsent(request)) {
            return ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400);
        }
        if (isNotSupportedAvailableAccounts(request)) {
            return ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400);
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
        if (isNotEmptyAccess(request.getAccess(), request.getAisConsentData())
                || Stream.of(request.getAvailableAccounts(), request.getAllPsd2(), request.getAvailableAccountsWithBalance()).anyMatch(EnumSet.of(ALL_ACCOUNTS, ALL_ACCOUNTS_WITH_OWNER_NAME)::contains)) {
            return false;
        }

        if (scaApproachResolver.resolveScaApproach() == EMBEDDED) {
            return true;
        }

        return !aspspProfileService.isBankOfferedConsentSupported();
    }

    private boolean isConsentGlobal(CreateConsentReq request) {
        return isNotEmptyAccess(request.getAccess(), request.getAisConsentData())
                   && EnumSet.of(ALL_ACCOUNTS, ALL_ACCOUNTS_WITH_OWNER_NAME).contains(request.getAllPsd2());
    }

    private boolean isNotEmptyAccess(AccountAccess access, AisConsentData aisConsentData) {
        return Optional.ofNullable(access)
                   .map(ac -> ac.isNotEmpty(aisConsentData))
                   .orElse(false);
    }

    private boolean isNotSupportedAvailableAccounts(CreateConsentReq request) {
        boolean isConsentWithoutAvailableAccounts = Stream.of(request.getAvailableAccounts(), request.getAvailableAccountsWithBalance())
                                                        .allMatch(Objects::isNull);

        if (isConsentWithoutAvailableAccounts) {
            return false;
        }

        return !aspspProfileService.isAvailableAccountsConsentSupported();
    }

    private boolean isNotSupportedCombinedServiceIndicator(CreateConsentReq request) {
        return request.isCombinedServiceIndicator()
                   && !aspspProfileService.isAisPisSessionsSupported();
    }
}
