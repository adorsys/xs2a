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

package de.adorsys.aspsp.xs2a.account;

import de.adorsys.aspsp.xs2a.consent.api.AccountInfo;
import de.adorsys.aspsp.xs2a.consent.api.TypeAccess;
import de.adorsys.aspsp.xs2a.domain.AccountAccess;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

@Value
public class AccountHolder {
    private final Map<String, Set<AccountAccess>> accountAccesses = new HashMap<>();

    public void fillAccess(List<AccountInfo> info, TypeAccess typeAccess) {
        if (CollectionUtils.isNotEmpty(info)) {
            for (AccountInfo a : info) {
                addAccountAccess(a.getIban(), getCurrencyByString(a.getCurrency()), typeAccess);
            }
        }
    }

    private Currency getCurrencyByString(String currency) {
        return Optional.ofNullable(currency)
                   .map(Currency::getInstance)
                   .orElse(null);
    }

    private void addAccountAccess(String iban, Currency currency, TypeAccess typeAccess) {
        accountAccesses.putIfAbsent(iban, new HashSet<>());
        accountAccesses.get(iban).add(new AccountAccess(currency, typeAccess));
        if (EnumSet.of(TypeAccess.BALANCE, TypeAccess.TRANSACTION).contains(typeAccess)) {
            accountAccesses.get(iban).add(new AccountAccess(currency, TypeAccess.ACCOUNT));
        }
    }
}
