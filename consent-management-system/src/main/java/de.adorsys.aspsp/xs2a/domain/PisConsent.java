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
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PisConsentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity(name = "pis_consent")
@ApiModel(description = "Pis consent entity", value = "PisConsent")
public class PisConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_consent_generator")
    @SequenceGenerator(name = "pis_consent_generator", sequenceName = "pis_consent_id_seq")
    private Long id;

    @Column(name = "external_id", nullable = false)
    @ApiModelProperty(value = "An external exposed identification of the created payment consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String externalId;

    @ElementCollection
    @CollectionTable(name = "pis_payments", joinColumns = @JoinColumn(name = "pis_consent_id"))
    @ApiModelProperty(value = "List of single payments ", required = true)
    private List<PisPaymentData> payments;

    @Column(name = "pis_consent_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the pis consent: BULK, SINGLE or PERIODIC.", required = true, example = "SINGLE")
    private PisConsentType pisConsentType;

    @Column(name = "consent_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the consent: AIS or PIS.", required = true, example = "AIS")
    private ConsentType consentType = ConsentType.PIS;

    @Column(name = "consent_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP.", required = true, example = "VALID")
    private SpiConsentStatus consentStatus;
}
