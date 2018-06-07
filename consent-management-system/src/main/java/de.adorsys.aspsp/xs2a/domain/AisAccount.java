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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Entity(name = "ais_account")
@ApiModel(description = "Ais account entity", value = "AisAccount")
public class AisAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_account_generator")
    @SequenceGenerator(name = "ais_account_generator", sequenceName = "ais_account_id_seq")
    private Long id;

    @Column(name = "iban", nullable = false)
    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", required = true, example = "DE2310010010123456789")
    private String iban;

    @ElementCollection
    @CollectionTable(name = "ais_account_access", joinColumns = @JoinColumn(name = "account_id"))
    @ApiModelProperty(value = "Set of accesses given by psu for this account", required = true)
    private List<AccountAccess> accesses = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", nullable = false)
    @ApiModelProperty(value = "Detailed information about consent", required = true)
    private AisConsent consent;

    public AisAccount() {}

    public AisAccount(String iban) {
        this.iban = iban;
    }

    public void addAccesses(Set<AccountAccess> accountAccesses){
        accesses.addAll(accountAccesses);
    }
}
