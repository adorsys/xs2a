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

import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString(exclude = "accounts")
@Entity(name = "ais_consent")
public class AisConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_consent_generator")
    @SequenceGenerator(name="ais_consent_generator", sequenceName = "ais_consent_id_seq")
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "recurring_indicator", nullable = false)
    private boolean recurringIndicator;

    @Column(name = "tpp_redirect_preferred", nullable = false)
    private boolean tppRedirectPreferred;

    @Column(name = "combined_service_indicator", nullable = false)
    private boolean combinedServiceIndicator;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @Column(name = "psu_id")
    private String psuId;

    @Column(name = "tpp_id", nullable = false)
    private String tppId;

    @Column(name = "consent_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private SpiConsentStatus consentStatus;

    @Column(name = "consent_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ConsentType consentType = ConsentType.AIS;

    @Column(name = "expected_frequency_per_day", nullable = false)
    private int expectedFrequencyPerDay;

    @Column(name = "tpp_frequency_per_day", nullable = false)
    private int tppFrequencyPerDay;

    @Column(name = "usage_counter", nullable = false)
    private int usageCounter;

    @OneToMany(mappedBy = "consent", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<AisAccount> accounts = new ArrayList<>();

    public void addAccounts(List<AisAccount> accounts) {
        accounts.forEach(a -> addAccount(a));
    }

    public void addAccount(AisAccount account) {
        this.accounts.add(account);
        account.setConsent(this);
    }
}
