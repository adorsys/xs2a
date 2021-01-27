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
