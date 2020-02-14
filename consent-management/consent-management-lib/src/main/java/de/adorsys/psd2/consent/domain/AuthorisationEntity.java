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

package de.adorsys.psd2.consent.domain;

import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity(name = "authorisation")
public class AuthorisationEntity extends InstanceDependableEntity {
    @Id
    @Column(name = "authorisation_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authorisation_generator")
    @SequenceGenerator(name = "authorisation_generator", sequenceName = "authorisation_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "psu_id")
    private PsuData psuData;

    @Column(name = "parent_id", nullable = false)
    private String parentExternalId;

    @Column(name = "sca_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ScaStatus scaStatus;

    @Column(name = "authentication_method_id")
    private String authenticationMethodId;

    @Column(name = "sca_authentication_data")
    private String scaAuthenticationData;

    @Column(name = "redirect_expiration_timestamp")
    private OffsetDateTime redirectUrlExpirationTimestamp;

    @Column(name = "expiration_timestamp")
    private OffsetDateTime authorisationExpirationTimestamp;

    @Column(name = "sca_approach", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ScaApproach scaApproach;

    @Column(name = "redirect_uri")
    private String tppOkRedirectUri;

    @Column(name = "nok_redirect_uri")
    private String tppNokRedirectUri;

    @ElementCollection
    @CollectionTable(name = "auth_available_sca_method", joinColumns = @JoinColumn(name = "authorisation_id"))
    private List<ScaMethod> availableScaMethods = new ArrayList<>();

    @Column(name = "authorisation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthorisationType authorisationType;

    public boolean isRedirectUrlNotExpired() {
        return redirectUrlExpirationTimestamp.isAfter(OffsetDateTime.now());
    }

    public boolean isAuthorisationNotExpired() {
        return authorisationExpirationTimestamp.isAfter(OffsetDateTime.now());
    }
}
