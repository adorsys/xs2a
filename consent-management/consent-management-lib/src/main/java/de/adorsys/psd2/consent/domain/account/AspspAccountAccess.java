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
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Currency;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "aspsp_account_access")
@EqualsAndHashCode
@ApiModel(description = "Aspsp Account access", value = "AspspAccountAccess")
public class AspspAccountAccess {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aspsp_account_access_generator")
    @SequenceGenerator(name = "aspsp_account_access_generator", sequenceName = "aspsp_account_access_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "consent_id", nullable = false)
    private ConsentEntity consent;

    @Column(name = "resource_id", length = 100)
    @ApiModelProperty(value = "RESOURCE-ID: This identification is denoting the addressed account.")
    private String resourceId;

    @Column(name = "aspsp_account_id", length = 100)
    @ApiModelProperty(value = "Aspsp-Account-ID: Bank specific account ID", example = "26bb59a3-2f63-4027-ad38-67d87e59611a")
    private String aspspAccountId;

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

    @Column(name = "account_reference_type", nullable = true, length = 30)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the account: IBAN, BBAN, IBAN, BBAN, PAN, MASKED_PAN, MSISDN", required = true, example = "IBAN")
    private AccountReferenceType accountReferenceType;

    public AspspAccountAccess(ConsentEntity consent, String accountIdentifier, TypeAccess typeAccess, AccountReferenceType accountReferenceType, Currency currency, String resourceId, String aspspAccountId) {
        this.consent = consent;
        this.resourceId = resourceId;
        this.aspspAccountId = aspspAccountId;
        this.accountIdentifier = accountIdentifier;
        this.typeAccess = typeAccess;
        this.accountReferenceType = accountReferenceType;
        this.currency = currency;
    }

    public AspspAccountAccess(Long id, ConsentEntity consent, String accountIdentifier, TypeAccess typeAccess, AccountReferenceType accountReferenceType, Currency currency, String resourceId, String aspspAccountId) {
        this.id = id;
        this.consent = consent;
        this.resourceId = resourceId;
        this.aspspAccountId = aspspAccountId;
        this.accountIdentifier = accountIdentifier;
        this.typeAccess = typeAccess;
        this.accountReferenceType = accountReferenceType;
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "AspspAccountAccess{" +
                   "id=" + id +
                   ", consentId=" + consent.getId() +
                   ", resourceId='" + resourceId + '\'' +
                   ", aspspAccountId='" + aspspAccountId + '\'' +
                   ", accountIdentifier='" + accountIdentifier + '\'' +
                   ", currency=" + currency +
                   ", typeAccess=" + typeAccess +
                   ", accountReferenceType=" + accountReferenceType +
                   '}';
    }
}
