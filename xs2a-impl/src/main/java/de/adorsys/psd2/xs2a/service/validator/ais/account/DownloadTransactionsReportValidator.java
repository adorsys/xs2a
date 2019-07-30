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

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.AbstractAisTppValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.DownloadTransactionListRequestObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_EXPIRED;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_401;

@Component
@RequiredArgsConstructor
public class DownloadTransactionsReportValidator extends AbstractAisTppValidator<DownloadTransactionListRequestObject> {

    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(DownloadTransactionListRequestObject consentObject) {
        AccountConsent accountConsent = consentObject.getAccountConsent();

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

        return ValidationResult.valid();
    }
}
