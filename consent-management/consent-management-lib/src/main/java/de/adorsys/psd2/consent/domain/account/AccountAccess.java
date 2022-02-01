/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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

    protected AccountAccess(String accountIdentifier, TypeAccess typeAccess, AccountReferenceType accountReferenceType, Currency currency) {
        this.accountIdentifier = accountIdentifier;
        this.typeAccess = typeAccess;
        this.accountReferenceType = accountReferenceType;
        this.currency = currency;
    }
}
