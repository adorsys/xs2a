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
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;

@Component
public class AccountAccessValidator {

    public ValidationResult validate(AisConsent aisConsent, boolean withBalance) {
        if (withBalance) {
            AisConsentData aisConsentData = aisConsent.getConsentData();

            if (aisConsentData.getAllPsd2() != null) {
                return ValidationResult.valid();
            }

            if (EnumSet.of(AccountAccessType.ALL_ACCOUNTS, AccountAccessType.ALL_ACCOUNTS_WITH_OWNER_NAME)
                    .contains(aisConsentData.getAvailableAccountsWithBalance())) {

                return ValidationResult.valid();
            }

            if (CollectionUtils.isEmpty(aisConsent.getAccess().getBalances())) {
                return ValidationResult.invalid(ErrorType.AIS_401, CONSENT_INVALID);
            }
        }

        return ValidationResult.valid();
    }
}
