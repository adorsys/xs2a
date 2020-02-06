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

import de.adorsys.psd2.consent.api.ConsentType;
import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.domain.*;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@ToString(exclude = {"accesses", "usages"})
@EqualsAndHashCode
@Entity(name = "ais_consent")
public class AisConsent extends InstanceDependableEntity implements Authorisable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_consent_generator")
    @SequenceGenerator(name = "ais_consent_generator", sequenceName = "ais_consent_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "recurring_indicator", nullable = false)
    private boolean recurringIndicator;

    @Column(name = "tpp_redirect_preferred", nullable = false)
    private boolean tppRedirectPreferred;

    @Column(name = "combined_service_indicator", nullable = false)
    private boolean combinedServiceIndicator;

    @Column(name = "request_date_time", nullable = false)
    private LocalDateTime requestDateTime;

    @Column(name = "last_action_date")
    private LocalDate lastActionDate;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    //TODO  Create migration file with not null constraint for "valid_until" https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1160
    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ais_consent_psu_data",
        joinColumns = @JoinColumn(name = "ais_consent_id"),
        inverseJoinColumns = @JoinColumn(name = "psu_data_id"))
    private List<PsuData> psuDataList = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tpp_info_id", nullable = false)
    private TppInfoEntity tppInfo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "authorisation_template_id", nullable = false)
    private AuthorisationTemplateEntity authorisationTemplate;

    @Column(name = "tpp_ntfc_uri")
    private String tppNotificationUri;

    @ElementCollection
    @CollectionTable(name = "ais_consent_tpp_ntfc", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "notification_mode", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private List<NotificationSupportedMode> tppNotificationContentPreferred;

    @Column(name = "consent_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ConsentStatus consentStatus;

    @Column(name = "consent_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ConsentType consentType = ConsentType.AIS;

    @Column(name = "expected_frequency_per_day", nullable = false)
    private int allowedFrequencyPerDay;

    @Column(name = "tpp_frequency_per_day", nullable = false)
    private int tppFrequencyPerDay;

    @OneToMany(mappedBy = "consent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AisConsentUsage> usages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ais_account_access", joinColumns = @JoinColumn(name = "consent_id"))
    private List<TppAccountAccess> accesses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ais_aspsp_account_access", joinColumns = @JoinColumn(name = "consent_id"))
    private List<AspspAccountAccess> aspspAccountAccesses = new ArrayList<>();

    @Column(name = "ais_consent_request_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AisConsentRequestType aisConsentRequestType;

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

    @Column(name = "multilevel_sca_required", nullable = false)
    private boolean multilevelScaRequired;

    @Column(name = "status_change_timestamp")
    private OffsetDateTime statusChangeTimestamp;

    @Column(name = "owner_name_type")
    @Enumerated(value = EnumType.STRING)
    private AdditionalAccountInformationType ownerNameType = AdditionalAccountInformationType.NONE;

    @Deprecated //TODO remove this column in 5.10 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1124
    @Column(name = "owner_address_type")
    @Enumerated(value = EnumType.STRING)
    private AdditionalAccountInformationType ownerAddressType = AdditionalAccountInformationType.NONE;

    @Column(name = "int_req_id")
    private String internalRequestId;

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

    public boolean isExpiredByDate() {
        return LocalDate.now().compareTo(validUntil) > 0;
    }

    public boolean isConfirmationExpired(long expirationPeriodMs) {
        if (isNotConfirmed()) {
            return creationTimestamp.plus(expirationPeriodMs, ChronoUnit.MILLIS)
                       .isBefore(OffsetDateTime.now());
        }

        return false;
    }

    public boolean isNotConfirmed() {
        return consentStatus == ConsentStatus.RECEIVED;
    }

    public void addAccountAccess(Set<TppAccountAccess> accountAccesses) {
        accesses = new ArrayList<>(accountAccesses);
    }

    public void addAspspAccountAccess(Set<AspspAccountAccess> aspspAccesses) {
        aspspAccountAccesses = new ArrayList<>(aspspAccesses);
    }

    public boolean isOneAccessType() {
        return !recurringIndicator;
    }

    public boolean isWrongConsentData() {
        return CollectionUtils.isEmpty(psuDataList)
                   || tppInfo == null;
    }

    public void addUsage(AisConsentUsage aisConsentUsage) {
        if (usages == null) {
            usages = new ArrayList<>();
        }
        usages.add(aisConsentUsage);
    }

    public boolean shouldConsentBeExpired() {
        return !this.getConsentStatus().isFinalisedStatus()
                   && (this.isExpiredByDate() || this.isNonReccuringAlreadyUsed());
    }

    /**
     * Checks, whether the consent is non-recurring and was used any time before today. Currently non-recurring consent
     * allows to perform read operations only within the day, it was used first time.
     *
     * @return Returns true if consent is non-recurrent and has no usages before today, false otherwise.
     */
    public boolean isNonReccuringAlreadyUsed() {
        return !recurringIndicator && usages.stream()
                                          .anyMatch(u -> u.getUsageDate().isBefore(LocalDate.now()));
    }

    public boolean checkNoneAdditionalAccountInformation() {
        return getOwnerNameType() == AdditionalAccountInformationType.NONE;
    }

    @Override
    public String getInternalRequestId(AuthorisationType authorisationType) {
        if (authorisationType == AuthorisationType.AIS) {
            return internalRequestId;
        }

        throw new IllegalArgumentException("Invalid authorisation type: " + authorisationType);
    }
}
