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

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;

@Component
public class AccountAccessMultipleAccountsValidator {

    public ValidationResult validate(AccountConsent accountConsent, boolean withBalance) {
        if (withBalance && accountConsent.isConsentForDedicatedAccounts()) {
            Xs2aAccountAccess access = accountConsent.getAccess();
            return validateAccountReferenceSize(access.getAccounts(), access.getBalances())
                       ? ValidationResult.invalid(ErrorType.AIS_401, CONSENT_INVALID)
                       : ValidationResult.valid();
        }

        return ValidationResult.valid();
    }

    private boolean validateAccountReferenceSize(List<AccountReference> accounts, List<AccountReference> balances) {
        return CollectionUtils.isNotEmpty(accounts)
                   && CollectionUtils.isNotEmpty(balances)
                   && accounts.size() > balances.size();
    }
}
