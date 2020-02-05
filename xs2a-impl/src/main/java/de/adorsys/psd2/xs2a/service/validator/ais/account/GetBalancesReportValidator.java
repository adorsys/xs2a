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

import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountBalanceRequestObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Validator to be used for validating get balances report request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class GetBalancesReportValidator extends AbstractAccountTppValidator<CommonAccountBalanceRequestObject> {
    private final AccountConsentValidator accountConsentValidator;
    private final AccountReferenceAccessValidator accountReferenceAccessValidator;


    /**
     * Validates get balances report request
     *
     * @param consentObject consent information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(CommonAccountBalanceRequestObject consentObject) {
        AccountConsent accountConsent = consentObject.getAccountConsent();
        Xs2aAccountAccess accountAccess = accountConsent.getAspspAccess();
        ValidationResult accountReferenceValidationResult = accountReferenceAccessValidator.validate(accountAccess,
                                                                                                     accountAccess.getBalances(),
                                                                                                     consentObject.getAccountId(),
                                                                                                     accountConsent.getAisConsentRequestType());
        if (accountReferenceValidationResult.isNotValid()) {
            return accountReferenceValidationResult;
        }

        return accountConsentValidator.validate(accountConsent, consentObject.getRequestUri());
    }
}
