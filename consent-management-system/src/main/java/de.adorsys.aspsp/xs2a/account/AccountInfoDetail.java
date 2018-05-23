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

import de.adorsys.aspsp.xs2a.domain.TypeAccess;
import lombok.Getter;
import lombok.Value;

import java.util.*;

import static java.util.Optional.ofNullable;

@Value
@Getter
public class AccountInfoDetail {
    private Map<String, InfoDetail> ibansAccess = new HashMap<>();

    public void addAccountAccess(String iban, Currency currency, Set<TypeAccess> typeAccesses){
        buildInfoDetail(iban, currency)
            .addAccess(typeAccesses);
    }

    public void addAccountAccess(String iban, Currency currency, TypeAccess typeAccess){
        buildInfoDetail(iban, currency)
            .addAccess(typeAccess);
    }

    private InfoDetail buildInfoDetail(String iban, Currency currency) {
        InfoDetail detail = ibansAccess.get(iban);
        if(detail == null){
            detail = new InfoDetail();
            ibansAccess.put(iban, detail);
        }
        detail.addCurrency(currency);
        return detail;
    }

    @Value
    @Getter
    public class InfoDetail {
        private Set<TypeAccess> accesses = new HashSet<>();
        private Set<Currency> currencies = new HashSet<>();

        public void addAccess(TypeAccess typeAccess){
            accesses.add(typeAccess);
            if(EnumSet.of(TypeAccess.BALANCE, TypeAccess.TRANSACTION).contains(typeAccess)){
                accesses.add(TypeAccess.ACCOUNT);
            }
        }

        public void addAccess(Set<TypeAccess> typeAccesses){
            accesses.addAll(typeAccesses);
        }

        public void addCurrency(Currency currency){
            ofNullable(currency).ifPresent(c -> currencies.add(currency));
        }

        public void refreshCurrency(Set<Currency> values){
            currencies.clear();
            values.forEach(c -> addCurrency(c));
        }
    }
}
