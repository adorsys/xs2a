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

package de.adorsys.psd2.consent.domain.payment;

import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.ScaMethod;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString(exclude = "paymentData")
@Entity(name = "pis_consent_authorization")
public class PisAuthorization extends InstanceDependableEntity {
    @Id
    @Column(name = "authorization_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_consent_authorization_generator")
    @SequenceGenerator(name = "pis_consent_authorization_generator", sequenceName = "pis_consent_auth_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "psu_id")
    private PsuData psuData;

    @Column(name = "sca_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ScaStatus scaStatus;

    @Column(name = "chosen_sca_method")
    private String chosenScaMethod;

    @Column(name = "authorization_type")
    @Enumerated(value = EnumType.STRING)
    private PaymentAuthorisationType authorizationType;

    @Column(name = "redirect_expiration_timestamp")
    private OffsetDateTime redirectUrlExpirationTimestamp;

    @Column(name = "expiration_timestamp")
    private OffsetDateTime authorisationExpirationTimestamp;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PisCommonPaymentData paymentData;

    @Column(name = "sca_authentication_data")
    private String scaAuthenticationData;

    @Column(name = "redirect_uri")
    private String tppOkRedirectUri;

    @Column(name = "nok_redirect_uri")
    private String tppNokRedirectUri;

    @ElementCollection
    @CollectionTable(name = "pis_available_sca_method", joinColumns = @JoinColumn(name = "authorisation_id"))
    private List<ScaMethod> availableScaMethods = new ArrayList<>();

    @Column(name = "sca_approach", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ScaApproach scaApproach;

    public boolean isRedirectUrlNotExpired() {
        return redirectUrlExpirationTimestamp.isAfter(OffsetDateTime.now());
    }

    public boolean isAuthorisationNotExpired() {
        return authorisationExpirationTimestamp.isAfter(OffsetDateTime.now());
    }
}
