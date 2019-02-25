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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.EMBEDDED;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccessType.ALL_ACCOUNTS;

@Component
@RequiredArgsConstructor
public class CreateConsentRequestValidator {
    private final AspspProfileServiceWrapper aspspProfileService;
    private final ScaApproachResolver scaApproachResolver;

    /**
     * Validates Create consent request according to:
     * <ul>
     * <li>supporting of global consent for All Psd2</li>
     * <li>supporting of bank offered consent</li>
     * <li>expiration date of consent</li>
     * </ul>
     * If there is new consent requirements, this method has to be updated.
     *
     * @param request CreateConsentReq request for consent creating
     * @return ValidationResult instance, that contains boolean isValid, that shows if request is valid
     * and MessageError for invalid case
     */
    public ValidationResult validateRequest(CreateConsentReq request) {
        if (isNotSupportedGlobalConsentForAllPsd2(request)) {
            return new ValidationResult(false, new MessageError(ErrorType.AIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED)));
        }
        if (isNotSupportedBankOfferedConsent(request)) {
            return new ValidationResult(false, new MessageError(ErrorType.AIS_405, TppMessageInformation.of(SERVICE_INVALID_405)));
        }
        if (!isValidExpirationDate(request.getValidUntil())) {
            return new ValidationResult(false, new MessageError(ErrorType.AIS_400, TppMessageInformation.of(PERIOD_INVALID)));
        }

        if (isNotValidFrequencyForRecurringIndicator(request.isRecurringIndicator(), request.getFrequencyPerDay())) {
            return new ValidationResult(false, new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR)));
        }

        if (isNotSupportedAvailableAccounts(request)) {
            return new ValidationResult(false, new MessageError(ErrorType.AIS_405, TppMessageInformation.of(SERVICE_INVALID_405)));
        }
        return new ValidationResult(true, null);
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

    private boolean isValidExpirationDate(LocalDate validUntil) {
        int consentLifetime = Math.abs(aspspProfileService.getConsentLifetime());
        return validUntil.isAfter(LocalDate.now()) && isValidConsentLifetime(consentLifetime, validUntil);
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

    private boolean isValidConsentLifetime(int consentLifetime, LocalDate validUntil) {
        return consentLifetime == 0 || validUntil.isBefore(LocalDate.now().plusDays(consentLifetime));
    }

    private boolean isNotValidFrequencyForRecurringIndicator(boolean recurringIndicator, int frequencyPerDay) {
        if (!recurringIndicator) {
            return frequencyPerDay > 1;
        }

        return false;
    }

    private boolean isNotSupportedAvailableAccounts(CreateConsentReq request) {
        if (Objects.isNull(request.getAccess().getAvailableAccounts())) {
            return false;
        }

        return !aspspProfileService.isAvailableAccountsConsentSupported();
    }
}
