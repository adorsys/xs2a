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
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @deprecated since 5.11, use {@link de.adorsys.psd2.consent.domain.consent.ConsentEntity} instead
 */
@Deprecated // TODO: complete AIS consent migration https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1209
@Data
@ToString(exclude = "accesses")
@EqualsAndHashCode
@Entity(name = "ais_consent")
public class AisConsent extends InstanceDependableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_consent_generator")
    @SequenceGenerator(name = "ais_consent_generator", sequenceName = "ais_consent_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "combined_service_indicator", nullable = false)
    private boolean combinedServiceIndicator;

    @Column(name = "consent_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ConsentStatus consentStatus;

    @ElementCollection
    @CollectionTable(name = "ais_account_access", joinColumns = @JoinColumn(name = "consent_id"))
    private List<TppAccountAccess> accesses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ais_aspsp_account_access", joinColumns = @JoinColumn(name = "consent_id"))
    private List<AspspAccountAccess> aspspAccountAccesses = new ArrayList<>();

    @Column(name = "creation_timestamp", nullable = false)
    private OffsetDateTime creationTimestamp = OffsetDateTime.now();

    @Column(name = "available_accounts")
    @Enumerated(value = EnumType.STRING)
    private AccountAccessType availableAccounts;

    @Column(name = "all_psd2")
    @Enumerated(value = EnumType.STRING)
    private AccountAccessType allPsd2;

    @Column(name = "accounts_with_balances")
    @Enumerated(value = EnumType.STRING)
    private AccountAccessType availableAccountsWithBalance;

    @Column(name = "status_change_timestamp")
    private OffsetDateTime statusChangeTimestamp;

    @Lob
    @Column(name = "checksum")
    private byte[] checksum;

    @Transient
    private ConsentStatus previousConsentStatus;

    @PostLoad
    public void aisConsentPostLoad() {
        previousConsentStatus = consentStatus;
    }

    @PreUpdate
    public void aisConsentPreUpdate() {
        if (previousConsentStatus != consentStatus) {
            statusChangeTimestamp = OffsetDateTime.now();
        }
    }

    @PrePersist
    public void aisConsentPrePersist() {
        if (Objects.isNull(statusChangeTimestamp)) {
            statusChangeTimestamp = creationTimestamp;
        }
    }

    public List<TppAccountAccess> getAccesses() {
        return new ArrayList<>(accesses);
    }
}
