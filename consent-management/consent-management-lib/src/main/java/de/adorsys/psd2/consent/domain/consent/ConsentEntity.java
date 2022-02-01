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

package de.adorsys.psd2.consent.domain.consent;

import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Data
@Entity(name = "consent")
@EqualsAndHashCode(callSuper = true)
public class ConsentEntity extends InstanceDependableEntity implements Authorisable {

    @Id
    @Column(name = "consent_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consent_generator")
    @SequenceGenerator(name = "consent_generator", sequenceName = "consent_id_seq",
        allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_status", nullable = false)
    private ConsentStatus consentStatus;

    @Column(name = "consent_type", nullable = false)
    private String consentType;

    @Column(name = "frequency_per_day", nullable = false)
    private int frequencyPerDay;

    @Column(name = "recurring_indicator", nullable = false)
    private boolean recurringIndicator;

    @Column(name = "multilevel_sca_required", nullable = false)
    private boolean multilevelScaRequired;

    @Lob
    @Column(name = "checksum")
    private byte[] checksum;

    @Lob
    @Column(name = "data")
    private byte[] data;

    @Column(name = "creation_timestamp", nullable = false)
    private OffsetDateTime creationTimestamp = OffsetDateTime.now();

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "last_action_date")
    private LocalDate lastActionDate;

    @Column(name = "request_date_time", nullable = false)
    private OffsetDateTime requestDateTime;

    @Column(name = "status_change_timestamp")
    private OffsetDateTime statusChangeTimestamp;

    @Column(name = "internal_request_id")
    private String internalRequestId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "authorisation_template_id", nullable = false)
    private AuthorisationTemplateEntity authorisationTemplate = new AuthorisationTemplateEntity();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "consent_tpp_information_id", nullable = false)
    private ConsentTppInformationEntity tppInformation = new ConsentTppInformationEntity();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "consent_psu_data",
        joinColumns = @JoinColumn(name = "consent_id"),
        inverseJoinColumns = @JoinColumn(name = "psu_data_id"))
    private List<PsuData> psuDataList = new ArrayList<>();

    @OneToMany(mappedBy = "consent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AisConsentUsage> usages = new ArrayList<>();

    @OneToMany(mappedBy = "consent", cascade = CascadeType.PERSIST)
    private List<TppAccountAccess> tppAccountAccesses = new ArrayList<>();

    @OneToMany(mappedBy = "consent", cascade = CascadeType.MERGE)
    private List<AspspAccountAccess> aspspAccountAccesses = new ArrayList<>();

    @Column(name = "owner_name_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AdditionalAccountInformationType ownerNameType = AdditionalAccountInformationType.NONE;

    @Column(name = "trusted_beneficiaries_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AdditionalAccountInformationType trustedBeneficiariesType = AdditionalAccountInformationType.NONE;

    @Column(name = "signing_basket_blocked", nullable = false)
    private boolean signingBasketBlocked;

    @Column(name = "signing_basket_authorised", nullable = false)
    private boolean signingBasketAuthorised;

    @Transient
    private ConsentStatus previousConsentStatus;

    @PostLoad
    public void consentPostLoad() {
        previousConsentStatus = consentStatus;
    }

    @PreUpdate
    public void consentPreUpdate() {
        if (previousConsentStatus != consentStatus) {
            statusChangeTimestamp = OffsetDateTime.now();
        }
    }

    @PrePersist
    public void consentPrePersist() {
        if (Objects.isNull(statusChangeTimestamp)) {
            statusChangeTimestamp = creationTimestamp;
        }
    }

    public boolean isConfirmationExpired(long expirationPeriodMs) {
        if (EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED).contains(consentStatus)) {
            return creationTimestamp.plus(expirationPeriodMs, ChronoUnit.MILLIS)
                       .isBefore(OffsetDateTime.now());
        }
        return false;
    }

    public void addUsage(AisConsentUsage aisConsentUsage) {
        if (usages == null) {
            usages = new ArrayList<>();
        }
        usages.add(aisConsentUsage);
    }

    public boolean isWrongConsentData() {
        return CollectionUtils.isEmpty(psuDataList)
                   || tppInformation == null
                   || tppInformation.getTppInfo() == null;
    }

    public boolean isExpiredByDate() {
        return LocalDate.now().compareTo(validUntil) > 0;
    }

    public boolean shouldConsentBeExpired() {
        if (ConsentType.getByValue(getConsentType()) == ConsentType.PIIS_TPP) {
            return false;
        }

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

    public boolean isOneAccessType() {
        return !recurringIndicator;
    }

    @Override
    public String getInternalRequestId(AuthorisationType authorisationType) {
        if (authorisationType == AuthorisationType.CONSENT) {
            return internalRequestId;
        }

        throw new IllegalArgumentException("Invalid authorisation type: " + authorisationType);
    }
}
