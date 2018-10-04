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

import de.adorsys.psd2.consent.api.TypeAccess;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Currency;

@Data
@Embeddable
@ApiModel(description = "Account access", value = "AccountAccess")
public class AccountAccess {
    @Column(name = "iban", nullable = false, length = 34)
    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", required = true, example = "DE2310010010123456789")
    private String iban;

    @Column(name = "currency", length = 3)
    @ApiModelProperty(value = "Currency Type", required = true, example = "EUR")
    private Currency currency;

    @Column(name = "type_access", nullable = false, length = 15)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Types of given accesses: account, balance, transaction, payment", required = true, example = "ACCOUNT")
    private TypeAccess typeAccess;

    public AccountAccess() {
    }

    public AccountAccess(String iban, Currency currency, TypeAccess typeAccess) {
        this.iban = iban;
        this.currency = currency;
        this.typeAccess = typeAccess;
    }
}
