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
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountAccessMultipleAccountsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetAccountListConsentObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;

/**
 * Validator to be used for validating get account list request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class GetAccountListValidator extends AbstractAccountTppValidator<GetAccountListConsentObject> {
    private final AccountConsentValidator accountConsentValidator;
    private final AccountAccessValidator accountAccessValidator;
    private final AccountAccessMultipleAccountsValidator accountAccessMultipleAccountsValidator;
    private final OauthConsentValidator oauthConsentValidator;

    /**
     * Validates get account list request  by checking whether:
     * <ul>
     * <li>consent has access to balances if the balance information was requested</li>
     * </ul>
     *
     * @param consentObject consent information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(GetAccountListConsentObject consentObject) {
        AisConsent aisConsent = consentObject.getAisConsent();

        if (aisConsent.isConsentWithNotIbanAccount() && !aisConsent.isConsentForAllAvailableAccounts() && !aisConsent.isGlobalConsent()) {
            return ValidationResult.invalid(AIS_401, CONSENT_INVALID);
        }

        ValidationResult accountConsentValidationResult = accountConsentValidator.validate(aisConsent, consentObject.getRequestUri());

        if (accountConsentValidationResult.isNotValid()) {
            return accountConsentValidationResult;
        }

        ValidationResult accountAccessValidationResult = accountAccessValidator.validate(aisConsent, consentObject.isWithBalance());
        if (accountAccessValidationResult.isNotValid()) {
            return accountAccessValidationResult;
        }

        ValidationResult accountAccessMultipleAccountsValidatorResult = accountAccessMultipleAccountsValidator.validate(aisConsent, consentObject.isWithBalance());
        if (accountAccessMultipleAccountsValidatorResult.isNotValid()) {
            return accountAccessMultipleAccountsValidatorResult;
        }

        ValidationResult oauthConsentValidationResult = oauthConsentValidator.validate(aisConsent);
        if (oauthConsentValidationResult.isNotValid()) {
            return oauthConsentValidationResult;
        }

        return ValidationResult.valid();
    }
}
