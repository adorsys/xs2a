/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
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
    private String resourceId;

    @Column(name = "aspsp_account_id", length = 100)
    private String aspspAccountId;

    @Column(name = "account_identifier", length = 34, nullable = false)
    private String accountIdentifier;

    @Column(name = "currency", length = 3)
    private Currency currency;

    @Column(name = "type_access", length = 30, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TypeAccess typeAccess;

    @Column(name = "account_reference_type", nullable = true, length = 30)
    @Enumerated(value = EnumType.STRING)
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
