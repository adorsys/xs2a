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

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.DownloadTransactionListRequestObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_EXPIRED;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;

@Component
@RequiredArgsConstructor
public class DownloadTransactionsReportValidator extends AbstractAccountTppValidator<DownloadTransactionListRequestObject> {

    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(DownloadTransactionListRequestObject consentObject) {
        AisConsent aisConsent = consentObject.getAisConsent();

        if (LocalDate.now().compareTo(aisConsent.getValidUntil()) > 0) {
            return ValidationResult.invalid(AIS_401, CONSENT_EXPIRED);
        }

        ConsentStatus consentStatus = aisConsent.getConsentStatus();
        if (consentStatus != ConsentStatus.VALID) {
            MessageErrorCode messageErrorCode = consentStatus == ConsentStatus.RECEIVED
                                                    ? CONSENT_INVALID
                                                    : CONSENT_EXPIRED;
            return ValidationResult.invalid(AIS_401, messageErrorCode);
        }

        return ValidationResult.valid();
    }
}
