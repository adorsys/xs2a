/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

@Data
@ToString(exclude = {"accesses", "authorizations", "usages"})
@Entity(name = "ais_consent")
@ApiModel(description = "Ais consent entity", value = "AisConsent")
public class AisConsent extends InstanceDependableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_consent_generator")
    @SequenceGenerator(name = "ais_consent_generator", sequenceName = "ais_consent_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false)
    @ApiModelProperty(value = "An external exposed identification of the created account consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String externalId;

    @Column(name = "recurring_indicator", nullable = false)
    @ApiModelProperty(value = "'true', if the consent is for recurring access to the account data , 'false', if the consent is for single access to the account data", required = true, example = "false")
    private boolean recurringIndicator;

    @Column(name = "tpp_redirect_preferred", nullable = false)
    @ApiModelProperty(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.", required = true, example = "false")
    private boolean tppRedirectPreferred;

    @Column(name = "combined_service_indicator", nullable = false)
    @ApiModelProperty(value = "'true' if aspsp supports combined sessions, otherwise 'false'.", required = true, example = "false")
    private boolean combinedServiceIndicator;

    @Column(name = "request_date_time", nullable = false)
    @ApiModelProperty(value = "Date of the last request for this consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2018-10-25T15:30:35.035Z")
    private LocalDateTime requestDateTime;

    @Column(name = "last_action_date")
    @ApiModelProperty(value = "Date of the last action for this consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2018-05-04")
    private LocalDate lastActionDate;

    @Column(name = "expire_date", nullable = false)
    @ApiModelProperty(value = "Expiration date for the requested consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2018-05-04")
    private LocalDate expireDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ais_consent_psu_data",
        joinColumns = @JoinColumn(name = "ais_consent_id"),
        inverseJoinColumns = @JoinColumn(name = "psu_data_id"))
    private List<PsuData> psuDataList = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tpp_info_id", nullable = false)
    @ApiModelProperty(value = "Information about TPP", required = true)
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
    @ApiModelProperty(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP by more values.", required = true, example = "VALID")
    private ConsentStatus consentStatus;

    @Column(name = "consent_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the consent: AIS or PIS.", required = true, example = "AIS")
    private ConsentType consentType = ConsentType.AIS;

    @Column(name = "expected_frequency_per_day", nullable = false)
    @ApiModelProperty(value = "Maximum frequency for an access per day, based on tppFrequencyPerDate and inner calculations. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private int allowedFrequencyPerDay;

    @Column(name = "tpp_frequency_per_day", nullable = false)
    @ApiModelProperty(value = "Requested maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private int tppFrequencyPerDay;

    @OneToMany(mappedBy = "consent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AisConsentUsage> usages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ais_account_access", joinColumns = @JoinColumn(name = "consent_id"))
    @ApiModelProperty(value = "Set of accesses given by psu for this account", required = true)
    private List<TppAccountAccess> accesses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ais_aspsp_account_access", joinColumns = @JoinColumn(name = "consent_id"))
    @ApiModelProperty(value = "Set of aspsp account accesses given by aspsp for this account", required = true)
    private List<AspspAccountAccess> aspspAccountAccesses = new ArrayList<>();

    @OneToMany(mappedBy = "consent", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @ApiModelProperty(value = "List of authorizations related to the consent", required = true)
    private List<AisConsentAuthorization> authorizations = new ArrayList<>();

    @Column(name = "ais_consent_request_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the consent request: GLOBAL, BANK_OFFERED or DEDICATED_ACCOUNTS.", required = true, example = "GLOBAL")
    private AisConsentRequestType aisConsentRequestType;

    @Column(name = "creation_timestamp", nullable = false)
    @ApiModelProperty(value = "Creation timestamp of the consent.", required = true, example = "2018-12-28T00:00:00Z")
    private OffsetDateTime creationTimestamp = OffsetDateTime.now();

    @Column(name = "available_accounts")
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the available accounts access type: ALL_ACCOUNTS.", example = "ALL_ACCOUNTS")
    private AccountAccessType availableAccounts;

    @Column(name = "all_psd2")
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the account access types.", example = "ALL_ACCOUNTS")
    private AccountAccessType allPsd2;

    @Column(name = "accounts_with_balances")
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the available accounts with balances access type: ALL_ACCOUNTS.", example = "ALL_ACCOUNTS")
    private AccountAccessType availableAccountsWithBalance;

    @Column(name = "multilevel_sca_required", nullable = false)
    private boolean multilevelScaRequired;

    @Column(name = "status_change_timestamp")
    private OffsetDateTime statusChangeTimestamp;

    @Column(name = "owner_name_type")
    @Enumerated(value = EnumType.STRING)
    private AdditionalAccountInformationType ownerNameType = AdditionalAccountInformationType.NONE;

    @Column(name = "owner_address_type")
    @Enumerated(value = EnumType.STRING)
    private AdditionalAccountInformationType ownerAddressType = AdditionalAccountInformationType.NONE;

    @Column(name = "int_req_id")
    private String internalRequestId;

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
        return LocalDate.now().compareTo(expireDate) > 0;
    }

    public boolean isConfirmationExpired(long expirationPeriodMs) {
        if (isNotConfirmed()) {
            return creationTimestamp.plus(expirationPeriodMs, ChronoUnit.MILLIS)
                       .isBefore(OffsetDateTime.now());
        }

        return false;
    }

    public boolean isStatusNotExpired() {
        return consentStatus != ConsentStatus.EXPIRED;
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
        return Stream.of(getOwnerNameType(), getOwnerAddressType()).allMatch(type -> type.equals(AdditionalAccountInformationType.NONE));
    }
}
