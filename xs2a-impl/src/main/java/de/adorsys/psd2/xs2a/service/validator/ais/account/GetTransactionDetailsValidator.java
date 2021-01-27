/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountTransactionsRequestObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Validator to be used for validating get transaction details request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class GetTransactionDetailsValidator extends AbstractAccountTppValidator<CommonAccountTransactionsRequestObject> {
    private final AccountConsentValidator accountConsentValidator;
    private final AccountReferenceAccessValidator accountReferenceAccessValidator;
    private final OauthConsentValidator oauthConsentValidator;

    /**
     * Validates get transaction details request
     *
     * @param consentObject consent information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(CommonAccountTransactionsRequestObject consentObject) {
        AisConsent aisConsent = consentObject.getAisConsent();
        AccountAccess accountAccess = consentObject.getAisConsent().getAccess();
        ValidationResult accountReferenceValidationResult = accountReferenceAccessValidator.validate(aisConsent,
                                                                                                     accountAccess.getTransactions(), consentObject.getAccountId(), aisConsent.getAisConsentRequestType());
        if (accountReferenceValidationResult.isNotValid()) {
            return accountReferenceValidationResult;
        }

        ValidationResult oauthConsentValidationResult = oauthConsentValidator.validate(aisConsent);
        if (oauthConsentValidationResult.isNotValid()) {
            return oauthConsentValidationResult;
        }

        return accountConsentValidator.validate(aisConsent, consentObject.getRequestUri());
    }
}
