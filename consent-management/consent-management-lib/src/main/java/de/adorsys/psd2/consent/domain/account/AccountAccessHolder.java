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
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

@Value
public class AccountAccessHolder {
    private final Set<AccountAccess> accountAccesses = new HashSet<>();

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
        accountAccesses.add(new AccountAccess(iban, currency, typeAccess));
        if (EnumSet.of(TypeAccess.BALANCE, TypeAccess.TRANSACTION).contains(typeAccess)) {
            accountAccesses.add(new AccountAccess(iban, currency, TypeAccess.ACCOUNT));
        }
    }
}
