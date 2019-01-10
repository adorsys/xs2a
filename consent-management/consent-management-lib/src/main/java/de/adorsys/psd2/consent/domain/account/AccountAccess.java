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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.util.Currency;

@Data
@NoArgsConstructor
@MappedSuperclass
public abstract class AccountAccess {
    @Column(name = "account_identifier", length = 34, nullable = false)
    @ApiModelProperty(value = "Account-Identifier: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE2310010010123456789", required = true)
    private String accountIdentifier;

    @Column(name = "currency", length = 3)
    @ApiModelProperty(value = "Currency Type", example = "EUR")
    private Currency currency;

    @Column(name = "type_access", length = 30, nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Types of given accesses: account, balance, transaction, payment", example = "ACCOUNT")
    private TypeAccess typeAccess;

    @Column(name = "account_reference_type", nullable = false, length = 30)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the account: IBAN, BBAN, IBAN, BBAN, PAN, MASKED_PAN, MSISDN", required = true, example = "IBAN")
    private AccountReferenceType accountReferenceType;

    public AccountAccess(String accountIdentifier, TypeAccess typeAccess, AccountReferenceType accountReferenceType, Currency currency) {
        this.accountIdentifier = accountIdentifier;
        this.typeAccess = typeAccess;
        this.accountReferenceType = accountReferenceType;
        this.currency = currency;
    }
}
