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

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthorisationType type;

    public boolean isRedirectUrlNotExpired() {
        return redirectUrlExpirationTimestamp.isAfter(OffsetDateTime.now());
    }

    public boolean isAuthorisationNotExpired() {
        return authorisationExpirationTimestamp.isAfter(OffsetDateTime.now());
    }
}
