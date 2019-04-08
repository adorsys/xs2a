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

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.EMBEDDED;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;

/**
 * Validator to be used for validating create consent request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class CreateConsentRequestValidator implements BusinessValidator<CreateConsentReq> {
    private final AspspProfileServiceWrapper aspspProfileService;
    private final ScaApproachResolver scaApproachResolver;

    /**
     * Validates Create consent request according to:
     * <ul>
     * <li>supporting of global consent for All Psd2</li>
     * <li>supporting of bank offered consent</li>
     * <li>expiration date of consent</li>
     * </ul>
     * If there are new consent requirements, this method has to be updated.
     *
     * @param request CreateConsentReq request for consent creating
     * @return ValidationResult instance, that contains boolean isValid, that shows if request is valid
     * and MessageError for invalid case
     */
    @NotNull
    @Override
    public ValidationResult validate(@NotNull CreateConsentReq request) {
        if (isNotSupportedGlobalConsentForAllPsd2(request)) {
            return ValidationResult.invalid(ErrorType.AIS_400, PARAMETER_NOT_SUPPORTED);
        }
        if (isNotSupportedBankOfferedConsent(request)) {
            return ValidationResult.invalid(ErrorType.AIS_405, SERVICE_INVALID_405);
        }
        if (isNotValidExpirationDate(request.getValidUntil())) {
            return ValidationResult.invalid(ErrorType.AIS_400, PERIOD_INVALID);
        }
        if (isNotValidFrequencyPerDay(request.isRecurringIndicator(), request.getFrequencyPerDay())) {
            return ValidationResult.invalid(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR, "Value of frequencyPerDay is not correct"));
        }
        if (isNotSupportedAvailableAccounts(request)) {
            return ValidationResult.invalid(ErrorType.AIS_405, SERVICE_INVALID_405);
        }
        if (areFlagsAndAccountsInvalid(request)) {
            return ValidationResult.invalid(ErrorType.AIS_400, FORMAT_ERROR);
        }
        return ValidationResult.valid();
    }

    private boolean areFlagsAndAccountsInvalid(CreateConsentReq request) {
        Xs2aAccountAccess access = request.getAccess();
        if (access.isNotEmpty()) {
            return !(CollectionUtils.isEmpty(request.getAccountReferences()) || areFlagsEmpty(access));
        }
        return false;
    }

    private boolean areFlagsEmpty(Xs2aAccountAccess access) {
        return Objects.isNull(access.getAvailableAccounts()) && Objects.isNull(access.getAllPsd2());
    }

    private boolean isNotSupportedGlobalConsentForAllPsd2(CreateConsentReq request) {
        return isConsentGlobal(request)
                   && !aspspProfileService.getAllPsd2Support();
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

    private boolean isNotValidExpirationDate(LocalDate validUntil) {
        return validUntil.isBefore(LocalDate.now());
    }

    private boolean isConsentGlobal(CreateConsentReq request) {
        return isNotEmptyAccess(request.getAccess())
                   && request.getAccess().getAllPsd2() == ALL_ACCOUNTS;
    }

    private Boolean isNotEmptyAccess(Xs2aAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(Xs2aAccountAccess::isNotEmpty)
                   .orElse(false);
    }

    private boolean isNotValidFrequencyPerDay(boolean recurringIndicator, int frequencyPerDay) {
        return recurringIndicator
                   ? frequencyPerDay <= 0
                   : frequencyPerDay != 1;
    }

    private boolean isNotSupportedAvailableAccounts(CreateConsentReq request) {
        if (Objects.isNull(request.getAccess().getAvailableAccounts())) {
            return false;
        }

        return !aspspProfileService.isAvailableAccountsConsentSupported();
    }
}
