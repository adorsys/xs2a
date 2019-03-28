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

package de.adorsys.psd2.xs2a.service.validator.ais.account.common;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_429;

@Component
public class AccountConsentValidator {

    public ValidationResult validate(AccountConsent accountConsent) {
        if (LocalDate.now().compareTo(accountConsent.getValidUntil()) > 0) {
            return ValidationResult.invalid(AIS_401, of(CONSENT_EXPIRED));
        }

        ConsentStatus consentStatus = accountConsent.getConsentStatus();
        if (consentStatus != ConsentStatus.VALID) {
            MessageErrorCode messageErrorCode = consentStatus == ConsentStatus.RECEIVED
                                                    ? CONSENT_INVALID
                                                    : CONSENT_EXPIRED;
            return ValidationResult.invalid(AIS_401, of(messageErrorCode));
        }

        if (accountConsent.isAccessExceeded()) {
            return ValidationResult.invalid(AIS_429, of(ACCESS_EXCEEDED));
        }

        return ValidationResult.valid();
    }
}
