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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString(exclude = "accounts")
@Entity(name = "ais_consent")
@ApiModel(description = "Ais consent entity", value = "AisConsent")
public class AisConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_consent_generator")
    @SequenceGenerator(name = "ais_consent_generator", sequenceName = "ais_consent_id_seq")
    private Long id;

    @Column(name = "external_id", nullable = false)
    @ApiModelProperty(value = "Set of accesses given by psu for this account", required = false, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String externalId;

    @Column(name = "recurring_indicator", nullable = false)
    @ApiModelProperty(value = "'true', if the consent is for recurring access to the account data , 'false', if the consent is for single access to the account data", required = true, example = "false")
    private boolean recurringIndicator;

    @Column(name = "tpp_redirect_preferred", nullable = false)
    @ApiModelProperty(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.", example = "false")
    private boolean tppRedirectPreferred;

    @Column(name = "combined_service_indicator", nullable = false)
    @ApiModelProperty(value = "'true' if aspsp supports combined sessions, otherwise 'false'.", required = true, example = "false")
    private boolean combinedServiceIndicator;

    @Column(name = "request_date", nullable = false)
    @ApiModelProperty(value = "Date of the last request for this consent. The content is the local ASPSP date in ISODate Format", required = true, example = "23.05.2018 16:52")
    private Instant requestDate;

    @Column(name = "expire_date", nullable = false)
    @ApiModelProperty(value = "Expiration date for the requested consent. The content is the local ASPSP date in ISODate Format", required = true, example = "23.05.2018 16:52")
    private Instant expireDate;

    @Column(name = "psu_id")
    @ApiModelProperty(value = "Psu id", required = true, example = "PSU_001")
    private String psuId;

    @Column(name = "tpp_id", nullable = false)
    @ApiModelProperty(value = "TPP id", required = true, example = "af006545-d713-46d7-b6cf-09c9628f9a5d")
    private String tppId;

    @Column(name = "consent_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP by more values.", required = true, example = "VALID")
    private SpiConsentStatus consentStatus;

    @Column(name = "consent_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the consent: AIS or PIS.", required = true, example = "AIS")
    private ConsentType consentType = ConsentType.AIS;

    @Column(name = "expected_frequency_per_day", nullable = false)
    @ApiModelProperty(value = "Maximum frequency for an access per day, based on tppFrequencyPerDate and inner calculations. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private int expectedFrequencyPerDay;

    @Column(name = "tpp_frequency_per_day", nullable = false)
    @ApiModelProperty(value = "Requested maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private int tppFrequencyPerDay;

    @Column(name = "usage_counter", nullable = false)
    @ApiModelProperty(value = "Usage counter for the consent", required = true, example = "7")
    private int usageCounter;

    @OneToMany(mappedBy = "consent", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @ApiModelProperty(value = "List of accounts related to the consent", required = true)
    private List<AisAccount> accounts = new ArrayList<>();

    public void addAccounts(List<AisAccount> accounts) {
        accounts.forEach(this::addAccount);
    }

    private void addAccount(AisAccount account) {
        this.accounts.add(account);
        account.setConsent(this);
    }

    public boolean isExpired() {
        return Instant.now()
                   .isAfter(expireDate);
    }
}
