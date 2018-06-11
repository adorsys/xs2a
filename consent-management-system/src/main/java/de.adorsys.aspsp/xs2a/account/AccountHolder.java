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
import de.adorsys.aspsp.xs2a.domain.AccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.TypeAccess;
import lombok.Getter;
import lombok.Value;

import java.util.*;

@Value
@Getter
public class AccountHolder {
    private Map<String, Set<AccountAccess>> accountAccesses = new HashMap<>();

    public void fillAccess(List<AccountInfo> info, TypeAccess typeAccess) {
        info = Optional.ofNullable(info).orElse(Collections.emptyList());
        info.forEach(a -> addAccountAccess(a.getIban(), getCurrencyByString(a.getCurrency()), typeAccess));
    }

    private Currency getCurrencyByString(String currency) {
        return Optional.ofNullable(currency)
                   .map(Currency::getInstance)
                   .orElse(null);
    }

    public void addAccountAccess(String iban, Currency currency, TypeAccess typeAccess) {
        Set<AccountAccess> accesses = Optional.ofNullable(accountAccesses.putIfAbsent(iban, new HashSet<>()))
                                          .orElse(new HashSet<>());
        accesses.add(new AccountAccess(currency, typeAccess));
        if (EnumSet.of(TypeAccess.BALANCE, TypeAccess.TRANSACTION).contains(typeAccess)) {
            accesses.add(new AccountAccess(currency, TypeAccess.ACCOUNT));
        }
    }
}
