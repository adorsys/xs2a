/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

import static de.adorsys.psd2.consent.api.TypeAccess.*;

@Value
public class AccountAccessHolder {
    private Set<AccountAccess> accountAccesses = new HashSet<>();
    private AisAccountAccessInfo accountAccessInfo;

    public AccountAccessHolder(AisAccountAccessInfo accountAccessInfo) {
        this.accountAccessInfo = accountAccessInfo;
        fillAccess(this.accountAccessInfo);
    }

    private void fillAccess(AisAccountAccessInfo accountAccessInfo) {
        doFillAccess(accountAccessInfo.getAccounts(), ACCOUNT);
        doFillAccess(accountAccessInfo.getBalances(), BALANCE);
        doFillAccess(accountAccessInfo.getTransactions(), TRANSACTION);
    }

    private void doFillAccess(List<AccountInfo> info, TypeAccess typeAccess) {
        if (CollectionUtils.isNotEmpty(info)) {
            for (AccountInfo a : info) {
                addAccountAccess(a.getResourceId(), a.getAccountIdentifier(), getCurrencyByString(a.getCurrency()), typeAccess, a.getAccountReferenceType());
            }
        }
    }

    private Currency getCurrencyByString(String currency) {
        return Optional.ofNullable(currency)
                   .map(Currency::getInstance)
                   .orElse(null);
    }

    private void addAccountAccess(String resourceId, String accountIdentifier, Currency currency, TypeAccess typeAccess, AccountReferenceType accountReferenceType) {
        accountAccesses.add(buildAccountAccess(resourceId, accountIdentifier, currency, accountReferenceType, typeAccess));
        if (EnumSet.of(BALANCE, TRANSACTION).contains(typeAccess)) {
            accountAccesses.add(buildAccountAccess(resourceId, accountIdentifier, currency, accountReferenceType, ACCOUNT));
        }
    }

    private AccountAccess buildAccountAccess(String resourceId, String accountIdentifier, Currency currency, AccountReferenceType accountReferenceType, TypeAccess typeAccess) {
        return new AccountAccess(accountIdentifier, typeAccess, accountReferenceType)
                   .resourceId(resourceId)
                   .currency(currency);
    }
}
