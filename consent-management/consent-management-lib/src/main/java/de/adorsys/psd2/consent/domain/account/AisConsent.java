/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
