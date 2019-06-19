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

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermittedAccountReferenceValidator {
    private final AccountReferenceAccessValidator accountReferenceAccessValidator;

    public ValidationResult validate(AccountConsent accountConsent, String accountId, boolean withBalance) {
        Xs2aAccountAccess accountAccess = accountConsent.getAccess();
        List<AccountReference> accountReferences = withBalance
                                                       ? accountAccess.getBalances()
                                                       : accountAccess.getAccounts();

        return accountReferenceAccessValidator.validate(accountAccess, accountReferences, accountId);
    }
}
