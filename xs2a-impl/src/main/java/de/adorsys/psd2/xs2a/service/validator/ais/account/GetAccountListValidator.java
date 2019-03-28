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

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.AbstractAisTppValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_INVALID;

/**
 * Validator to be used for validating get account list request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class GetAccountListValidator extends AbstractAisTppValidator<GetAccountListConsentObject> {
    private final AccountConsentValidator accountConsentValidator;

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
        ValidationResult accountConsentValidationResult = accountConsentValidator.validate(consentObject.getAccountConsent());

        if (accountConsentValidationResult.isNotValid()) {
            return accountConsentValidationResult;
        }

        if (hasInvalidAccess(consentObject)) {
            return ValidationResult.invalid(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));
        }

        return ValidationResult.valid();
    }

    private boolean hasInvalidAccess(GetAccountListConsentObject consentObject) {
        if (consentObject.isWithBalance()) {
            Xs2aAccountAccess accountAccess = consentObject.getAccountConsent().getAccess();

            return accountAccess == null
                       || CollectionUtils.isEmpty(accountAccess.getBalances());
        }

        return false;
    }
}
