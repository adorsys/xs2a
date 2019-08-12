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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
public class AisConsentRequestTypeService {

    public AisConsentRequestType getRequestTypeFromConsent(AisConsent aisConsent) {
        return getRequestType(aisConsent.getAllPsd2(),
                              aisConsent.getAvailableAccounts(),
                              aisConsent.getAvailableAccountsWithBalance(),
                              aisConsent.getAspspAccountAccesses().isEmpty());
    }

    public AisConsentRequestType getRequestTypeFromAccess(AisAccountAccessInfo accessInfo) {
        return getRequestType(accessInfo.getAllPsd2(),
                              accessInfo.getAvailableAccounts(),
                              accessInfo.getAvailableAccountsWithBalance(),
                              isEmptyAccess(accessInfo));
    }

    private boolean isEmptyAccess(AisAccountAccessInfo accessInfo) {
        return CollectionUtils.isEmpty(accessInfo.getAccounts())
                   && CollectionUtils.isEmpty(accessInfo.getBalances())
                   && CollectionUtils.isEmpty(accessInfo.getTransactions());
    }

    private AisConsentRequestType getRequestType(AccountAccessType allPsd2,
                                                 AccountAccessType availableAccounts,
                                                 AccountAccessType availableAccountsWithBalance,
                                                 boolean isAccessesEmpty) {

        if (AccountAccessType.ALL_ACCOUNTS == allPsd2) {
            return AisConsentRequestType.GLOBAL;
        } else if (AccountAccessType.ALL_ACCOUNTS == availableAccounts) {
            return AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
        } else if (AccountAccessType.ALL_ACCOUNTS == availableAccountsWithBalance) {
            return AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
        } else if (isAccessesEmpty) {
            return AisConsentRequestType.BANK_OFFERED;
        }
        return AisConsentRequestType.DEDICATED_ACCOUNTS;
    }
}
