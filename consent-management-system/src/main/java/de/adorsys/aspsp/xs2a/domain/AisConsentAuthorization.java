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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity(name = "ais_consent_authorization")
@ApiModel(description = "Ais consent authorization entity", value = "AisConsentAuthorization")
public class AisConsentAuthorization {
    @Id
    @Column(name = "authorization_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_consent_authorization_generator")
    @SequenceGenerator(name = "ais_consent_authorization_generator", sequenceName = "ais_consent_authorization_id_seq")
    private Long id;

    @Column(name = "external_id", nullable = false)
    @ApiModelProperty(value = "An external exposed identification of the created consent authorization", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String externalId;

    @Column(name = "psu_id")
    @ApiModelProperty(value = "Psu id", required = true, example = "PSU_001")
    private String psuId;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "consent_id")
    private AisConsent consent;

    @Column(name = "sca_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised' 'failed' 'exempted'.", required = true, example = "STARTED")
    private CmsScaStatus scaStatus;

    @Column(name = "authentication_method_id")
    @ApiModelProperty(value = "An identification provided by the ASPSP for the later identification of the authentication method selection.")
    private String authenticationMethodId;

    @Column(name = "sca_authentication_data")
    @ApiModelProperty(value = "SCA authentication data, depending on the chosen authentication method. If the data is binary, then it is base64 encoded.")
    private String scaAuthenticationData;

    @Column(name = "password")
    @ApiModelProperty(value = "password")
    private String password;

}
