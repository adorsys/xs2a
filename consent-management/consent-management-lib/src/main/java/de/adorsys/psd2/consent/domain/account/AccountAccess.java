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

import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
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
//TODO refactor class and change DB scheme https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/491
public class AccountAccess {

    @Column(name = "resource_id")
    @ApiModelProperty(value = "RESOURCE-ID: This identification is denoting the addressed account.")
    private String resourceId;

    @Column(name = "aspsp_account_id", length = 34)
    @ApiModelProperty(value = "Aspsp-Account-ID: Bank specific account ID", example = "DE2310010010123456789")
    private String aspspAccountId;

    @Column(name = "account_identifier", length = 34, nullable = false)
    @ApiModelProperty(value = "Account-Identifier: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE2310010010123456789", required = true)
    private String accountIdentifier;

    @Column(name = "currency", length = 3)
    @ApiModelProperty(value = "Currency Type", example = "EUR")
    private Currency currency;

    @Column(name = "type_access", length = 15, nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Types of given accesses: account, balance, transaction, payment", example = "ACCOUNT")
    private TypeAccess typeAccess;

    @Column(name = "account_reference_type", nullable = false, length = 15)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the account reference: IBAN, BBAN, IBAN, BBAN, PAN, MASKED_PAN, MSISDN", required = true, example = "IBAN")
    private AccountReferenceType accountReferenceType;

    public AccountAccess() {
    }

    public AccountAccess(String accountIdentifier, TypeAccess typeAccess, AccountReferenceType accountReferenceType) {
        this.accountIdentifier = accountIdentifier;
        this.typeAccess = typeAccess;
        this.accountReferenceType = accountReferenceType;
    }

    public AccountAccess resourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public AccountAccess aspspAccountId(String aspspAccountId) {
        this.aspspAccountId = aspspAccountId;
        return this;
    }

    public AccountAccess currency(Currency currency) {
        this.currency = currency;
        return this;
    }
}
