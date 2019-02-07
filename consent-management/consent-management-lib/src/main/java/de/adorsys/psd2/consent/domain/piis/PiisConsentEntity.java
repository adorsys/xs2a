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

package de.adorsys.psd2.consent.domain.piis;

import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.piis.PiisConsentTppAccessType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity(name = "piis_consent")
@ApiModel(description = "Piis consent entity", value = "PiisConsentEntity")
public class PiisConsentEntity extends InstanceDependableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "piis_consent_generator")
    @SequenceGenerator(name = "piis_consent_generator", sequenceName = "piis_consent_id_seq")
    private Long id;

    @Column(name = "external_id", nullable = false)
    @ApiModelProperty(value = "An external exposed identification of the created PIIS consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String externalId;

    @Column(name = "recurring_indicator", nullable = false)
    @ApiModelProperty(value = "'true', if the consent is for recurring access to the account data , 'false', if the consent is for single access to the account data", required = true, example = "false")
    private boolean recurringIndicator;

    @Column(name = "request_date_time", nullable = false)
    @ApiModelProperty(value = "Date of the last request for this consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2018-10-25T15:30:35.035Z")
    private OffsetDateTime requestDateTime;

    @Column(name = "last_action_date")
    @ApiModelProperty(value = "Date of the last action for this consent. The content is the local ASPSP date in ISODate Format", example = "2018-05-04")
    private LocalDate lastActionDate;

    @Column(name = "expire_date")
    @ApiModelProperty(value = "Expiration date for the requested consent. The content is the local ASPSP date in ISODate Format", example = "2018-05-04")
    private LocalDate expireDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "psu_id")
    private PsuData psuData;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tpp_info_id")
    @ApiModelProperty(value = "Information about TPP")
    private TppInfoEntity tppInfo;

    @Column(name = "consent_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP by more values.", required = true, example = "VALID")
    private ConsentStatus consentStatus;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "piis_consent_acc_reference",
        joinColumns = @JoinColumn(name = "piis_consent_id"),
        inverseJoinColumns = @JoinColumn(name = "account_reference_id"))
    private List<AccountReferenceEntity> accounts = new ArrayList<>();

    @Column(name = "tpp_access_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the tpp access: SINGLE_TPP or ALL_TPP.", required = true, example = "ALL_TPP")
    private PiisConsentTppAccessType tppAccessType;

    @Column(name = "allowed_frequency_per_day", nullable = false)
    @ApiModelProperty(value = "Maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private int allowedFrequencyPerDay;

    @Column(name = "creation_timestamp", nullable = false)
    private OffsetDateTime creationTimestamp = OffsetDateTime.now();
}
