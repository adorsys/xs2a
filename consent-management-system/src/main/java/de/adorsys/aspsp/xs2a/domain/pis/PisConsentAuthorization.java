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

package de.adorsys.aspsp.xs2a.domain.pis;

import de.adorsys.aspsp.xs2a.consent.api.CmsScaMethod;
import de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString(exclude = "consent")
@Entity(name = "pis_consent_authorization")
public class PisConsentAuthorization {
    @Id
    @Column(name = "authorization_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_consent_authorization_generator")
    @SequenceGenerator(name = "pis_consent_authorization_generator", sequenceName = "pis_consent_authorization_id_seq")
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id")
    private PisConsent consent;

    @Column(name = "sca_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CmsScaStatus scaStatus;

    @Column(name = "chosen_sca_method")
    @Enumerated(value = EnumType.STRING)
    private CmsScaMethod chosenScaMethod;
}
