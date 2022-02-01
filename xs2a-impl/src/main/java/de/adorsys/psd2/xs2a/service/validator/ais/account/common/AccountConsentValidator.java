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

package de.adorsys.psd2.xs2a.service.validator.ais.account.common;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_429;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Component
@RequiredArgsConstructor
public class AccountConsentValidator {
    private final RequestProviderService requestProviderService;

    public ValidationResult validate(AisConsent aisConsent, String requestUri) {
        if (LocalDate.now().compareTo(aisConsent.getValidUntil()) > 0) {
            return ValidationResult.invalid(AIS_401, CONSENT_EXPIRED);
        }

        ConsentStatus consentStatus = aisConsent.getConsentStatus();
        if (consentStatus != ConsentStatus.VALID) {
            return processConsentInvalidStatus(consentStatus);
        }

        if (isAccessExceeded(aisConsent, requestUri)) {
            return ValidationResult.invalid(AIS_429, ACCESS_EXCEEDED);
        }

        return ValidationResult.valid();
    }

    private ValidationResult processConsentInvalidStatus(ConsentStatus consentStatus) {
        if (consentStatus == ConsentStatus.REVOKED_BY_PSU) {
            return ValidationResult.invalid(AIS_401, CONSENT_INVALID_REVOKED);
        }
        MessageErrorCode messageErrorCode = consentStatus == ConsentStatus.RECEIVED ? CONSENT_INVALID : CONSENT_EXPIRED;
        return ValidationResult.invalid(AIS_401, messageErrorCode);
    }

    private boolean isAccessExceeded(AisConsent aisConsent, String requestUri) {
        if (requestProviderService.isRequestFromPsu() && !aisConsent.isOneAccessType()) {
            return false;
        }

        if (!aisConsent.getUsageCounterMap().containsKey(requestUri)) {
            return false;
        }
        return aisConsent.getUsageCounterMap().get(requestUri) <= 0;
    }
}
