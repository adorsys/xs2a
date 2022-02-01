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

import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

/**
 * @deprecated since 5.11, use {@link de.adorsys.psd2.consent.domain.consent.ConsentEntity} instead
 */
// TODO: complete AIS consent migration https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1209
@Deprecated(since = "5.11", forRemoval = true)
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity(name = "ais_consent")
public class AisConsent extends InstanceDependableEntity {
    @Id
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "combined_service_indicator", nullable = false)
    private boolean combinedServiceIndicator;

    @Column(name = "available_accounts")
    @Enumerated(value = EnumType.STRING)
    private AccountAccessType availableAccounts;

    @Column(name = "all_psd2")
    @Enumerated(value = EnumType.STRING)
    private AccountAccessType allPsd2;

    @Column(name = "accounts_with_balances")
    @Enumerated(value = EnumType.STRING)
    private AccountAccessType availableAccountsWithBalance;
}
