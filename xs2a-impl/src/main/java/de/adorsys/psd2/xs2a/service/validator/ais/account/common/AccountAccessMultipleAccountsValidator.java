/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.validator.ais.account.common;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;

@Component
public class AccountAccessMultipleAccountsValidator {

    public ValidationResult validate(AisConsent aisConsent, boolean withBalance) {
        if (withBalance && aisConsent.isConsentForDedicatedAccounts()) {
            AccountAccess access = aisConsent.getAccess();
            return validateAccountReferenceSize(access.getAccounts(), access.getBalances())
                       ? ValidationResult.invalid(ErrorType.AIS_401, CONSENT_INVALID)
                       : ValidationResult.valid();
        }

        return ValidationResult.valid();
    }

    private boolean validateAccountReferenceSize(List<AccountReference> accounts, List<AccountReference> balances) {
        // This filtering allows to skip all card accounts (with masked PAN or PAN inside in case of ) and allows the further flow
        // to be finished.
        List<AccountReference> filteredAccountReferences = accounts.stream()
                                                               .filter(AccountReference::isNotCardAccount)
                                                               .collect(Collectors.toList());

        return CollectionUtils.isNotEmpty(filteredAccountReferences)
                   && CollectionUtils.isNotEmpty(balances)
                   && filteredAccountReferences.size() > balances.size();
    }
}
