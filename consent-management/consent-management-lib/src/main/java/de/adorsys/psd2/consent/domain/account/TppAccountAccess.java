/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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

import jakarta.persistence.*;
import java.util.Currency;

@Getter
@Setter
@Entity(name = "account_access")
@EqualsAndHashCode
@NoArgsConstructor
public class TppAccountAccess {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_access_generator")
    @SequenceGenerator(name = "account_access_generator", sequenceName = "account_access_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "consent_id", nullable = false)
    private ConsentEntity consent;

    @Column(name = "account_identifier", length = 34, nullable = false)
    private String accountIdentifier;

    @Column(name = "currency", length = 3)
    private Currency currency;

    @Column(name = "type_access", length = 30, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TypeAccess typeAccess;

    @Column(name = "account_reference_type", nullable = false, length = 30)
    @Enumerated(value = EnumType.STRING)
    private AccountReferenceType accountReferenceType;

    public TppAccountAccess(Long id, ConsentEntity consent, String accountIdentifier, TypeAccess typeAccess, AccountReferenceType accountReferenceType, Currency currency) {
        this.id = id;
        this.consent = consent;
        this.accountIdentifier = accountIdentifier;
        this.typeAccess = typeAccess;
        this.accountReferenceType = accountReferenceType;
        this.currency = currency;
    }
}
