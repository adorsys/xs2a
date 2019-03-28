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
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_INVALID;

@Component
@RequiredArgsConstructor
public class PermittedAccountReferenceValidator {

    public ValidationResult validate(AccountConsent accountConsent, List<AccountReference> references, String accountId, boolean withBalance) {
        if (isNotPermittedAccountReference(findAccountReference(references, accountId), accountConsent.getAccess(), withBalance)) {
            return ValidationResult.invalid(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));
        }

        return ValidationResult.valid();
    }

    private AccountReference findAccountReference(List<AccountReference> references, String accountId) {
        return references.stream()
                   .filter(accountReference -> StringUtils.equals(accountReference.getResourceId(), accountId))
                   .findFirst()
                   .orElse(null);
    }

    private boolean isNotPermittedAccountReference(AccountReference requestedAccountReference, Xs2aAccountAccess consentAccountAccess, boolean withBalance) {
        if (Objects.isNull(requestedAccountReference)) {
            return true;
        }

        List<AccountReference> accountReferences = withBalance
                                                       ? consentAccountAccess.getBalances()
                                                       : consentAccountAccess.getAccounts();

        return !isValidAccountByAccess(requestedAccountReference.getResourceId(), accountReferences);
    }

    private boolean isValidAccountByAccess(String accountId, List<AccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> a.getResourceId().equals(accountId));
    }
}
