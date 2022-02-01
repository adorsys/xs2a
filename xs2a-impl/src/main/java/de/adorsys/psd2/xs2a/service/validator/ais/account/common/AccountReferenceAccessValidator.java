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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;

@Component
@RequiredArgsConstructor
public class AccountReferenceAccessValidator {

    public ValidationResult validate(AisConsent aisConsent, List<AccountReference> references, String accountId, AisConsentRequestType consentRequestType) {
        if (AisConsentRequestType.GLOBAL == consentRequestType) {
            return ValidationResult.valid();
        }

        if (isConsentForAllAvailableAccounts(aisConsent)
                || !isValidAccountByAccess(accountId, references)) {
            return ValidationResult.invalid(ErrorType.AIS_401, CONSENT_INVALID);
        }

        return ValidationResult.valid();
    }

    private boolean isValidAccountByAccess(String accountId, List<AccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> StringUtils.equals(a.getResourceId(), accountId));
    }

    private boolean isConsentForAllAvailableAccounts(AisConsent aisConsent) {
        AisConsentData consentData = aisConsent.getConsentData();
        return Arrays.asList(consentData.getAvailableAccounts(), consentData.getAvailableAccountsWithBalance())
                   .contains(AccountAccessType.ALL_ACCOUNTS);
    }
}
