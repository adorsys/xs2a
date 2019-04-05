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
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.AbstractAisTppValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.PermittedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountRequestObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Validator to be used for validating get account details request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class GetAccountDetailsValidator extends AbstractAisTppValidator<CommonAccountRequestObject> {
    private final PermittedAccountReferenceValidator permittedAccountReferenceValidator;
    private final AccountConsentValidator accountConsentValidator;
    private final AccountAccessValidator accountAccessValidator;

    /**
     * Validates get account details request
     *
     * @param commonAccountRequestObject account request object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(CommonAccountRequestObject commonAccountRequestObject) {
        AccountConsent accountConsent = commonAccountRequestObject.getAccountConsent();

        ValidationResult permittedAccountReferenceValidationResult =
            permittedAccountReferenceValidator.validate(accountConsent, commonAccountRequestObject.getAccounts(), commonAccountRequestObject.getAccountId(), commonAccountRequestObject.isWithBalance());

        if (permittedAccountReferenceValidationResult.isNotValid()) {
            return permittedAccountReferenceValidationResult;
        }

        ValidationResult accountAccessValidationResult = accountAccessValidator.validate(commonAccountRequestObject.getAccountConsent(), commonAccountRequestObject.isWithBalance());
        if (accountAccessValidationResult.isNotValid()) {
            return accountAccessValidationResult;
        }

        return accountConsentValidator.validate(accountConsent);
    }
}
