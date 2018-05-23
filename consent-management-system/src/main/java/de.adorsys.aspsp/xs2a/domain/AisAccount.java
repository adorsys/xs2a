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

package de.adorsys.aspsp.xs2a.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity(name = "ais_account")
public class AisAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_account_generator")
    @SequenceGenerator(name="ais_account_generator", sequenceName = "ais_account_id_seq")
    private Long id;

    @Column(name = "iban", nullable = false)
    private String iban;

    @ElementCollection
    @CollectionTable(name="ais_account_currency", joinColumns=@JoinColumn(name="account_id"))
    private Set<AisAccountCurrency> aisAccountCurrencies = new HashSet<>();

    @ElementCollection
    @CollectionTable(name="ais_account_access", joinColumns=@JoinColumn(name="account_id"))
    private Set<AisAccountAccess> aisAccountAccesses = new HashSet<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", nullable = false)
    private AisConsent consent;

    public AisAccount() {}

    public AisAccount(String iban) {
        this.iban = iban;
    }

    public void addAccesses(Set<TypeAccess> typeAccesses){
        typeAccesses.forEach(t -> addAccess(t));
    }

    public void addAccess(TypeAccess typeAccess){
        aisAccountAccesses.add(new AisAccountAccess(typeAccess));
    }

    public void addCurrencies(Set<Currency> currencies){
        currencies.forEach(c -> addCurrency(c));
    }

    public void addCurrency(Currency currency){
        aisAccountCurrencies.add(new AisAccountCurrency(currency));
    }
}
