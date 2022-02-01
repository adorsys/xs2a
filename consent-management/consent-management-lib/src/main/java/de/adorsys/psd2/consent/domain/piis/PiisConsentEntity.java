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

package de.adorsys.psd2.consent.domain.piis;

import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * @deprecated since 5.11, use {@link de.adorsys.psd2.consent.domain.consent.ConsentEntity} for new consents instead
 */
// TODO: complete PIIS consent migration https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1216
@Deprecated(since = "5.11", forRemoval = true)
@Data
@Entity(name = "piis_consent")
@EqualsAndHashCode(callSuper = true)
public class PiisConsentEntity extends InstanceDependableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "piis_consent_generator")
    @SequenceGenerator(name = "piis_consent_generator", sequenceName = "piis_consent_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "recurring_indicator", nullable = false)
    private boolean recurringIndicator;

    @Column(name = "request_date_time", nullable = false)
    private OffsetDateTime requestDateTime;

    @Column(name = "last_action_date")
    private LocalDate lastActionDate;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "psu_id")
    private PsuData psuData;

    @Column(name = "consent_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ConsentStatus consentStatus;

    @JoinColumn(name = "acc_reference_id")
    @ManyToOne(cascade = CascadeType.ALL)
    private AccountReferenceEntity account;

    @Column(name = "creation_timestamp", nullable = false)
    private OffsetDateTime creationTimestamp = OffsetDateTime.now();

    @Column(name = "card_number", length = 35)
    private String cardNumber;

    @Column(name = "card_expiry_date")
    private LocalDate cardExpiryDate;

    @Column(name = "card_information", length = 140)
    private String cardInformation;

    @Column(name = "registration_information", length = 140)
    private String registrationInformation;

    @Column(name = "status_change_timestamp")
    private OffsetDateTime statusChangeTimestamp;

    @Column(name = "tpp_authorisation_number")
    private String tppAuthorisationNumber;

    @Transient
    private ConsentStatus previousConsentStatus;

    @PostLoad
    public void piisConsentPostLoad() {
        previousConsentStatus = consentStatus;
    }

    @PreUpdate
    public void piisConsentPreUpdate() {
        if (previousConsentStatus != consentStatus) {
            statusChangeTimestamp = OffsetDateTime.now();
        }
    }

    @PrePersist
    public void piisConsentPrePersist() {
        if (Objects.isNull(statusChangeTimestamp)) {
            statusChangeTimestamp = creationTimestamp;
        }
    }
}
