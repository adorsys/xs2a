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

import de.adorsys.aspsp.xs2a.domain.AccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AccountInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.TypeAccess;
import lombok.Getter;
import lombok.Value;

import java.util.*;

@Value
@Getter
public class AccountHolder {
    private Map<String, AccessInfo> ibansAccess = new HashMap<>();

    public void fillAccess(List<AccountInfo> info, TypeAccess typeAccess) {
        info = Optional.ofNullable(info).orElse(Collections.emptyList());
        info.forEach(a -> addAccountAccess(a.getIban(), getCurrency(a.getCurrency()), typeAccess));
    }

    private Currency getCurrency(String currency) {
        return Optional.ofNullable(currency)
                   .map(Currency::getInstance)
                   .orElse(null);
    }

    public void addAccountAccess(String iban, Currency currency, Set<TypeAccess> accesses) {
        buildInfoDetail(iban)
            .addAccess(currency, accesses);
    }

    public void addAccountAccess(String iban, Currency currency, TypeAccess access) {
        buildInfoDetail(iban)
            .addAccess(currency, access);
    }

    // TODO: putIfAbsent(iban, value); https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/122
    private AccessInfo buildInfoDetail(String iban) {
        AccessInfo detail = ibansAccess.get(iban);
        if (detail == null) {
            detail = new AccessInfo();
            ibansAccess.put(iban, detail);
        }
        return detail;
    }

    @Value
    @Getter
    public class AccessInfo {
        private Set<AccountAccess> accesses = new HashSet<>();

        private void addAccess(Currency currency, Set<TypeAccess> typeAccesses) {
            typeAccesses.forEach(t -> addAccess(currency, t));
        }

        private void addAccess(Currency currency, TypeAccess typeAccess) {
            accesses.add(new AccountAccess(currency, typeAccess));
            if (EnumSet.of(TypeAccess.BALANCE, TypeAccess.TRANSACTION).contains(typeAccess)) {
                accesses.add(new AccountAccess(currency, TypeAccess.ACCOUNT));
            }
        }

        public void updateAccess(Set<AccountAccess> newAccesses) {
            accesses.clear();
            accesses.addAll(newAccesses);
        }
    }
}
