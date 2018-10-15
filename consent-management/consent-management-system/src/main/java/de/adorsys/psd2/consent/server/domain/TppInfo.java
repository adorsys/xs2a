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

package de.adorsys.psd2.consent.server.domain;

import de.adorsys.psd2.consent.api.CmsTppRole;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity(name = "tpp_info")
@ApiModel(description = "Tpp info", value = "TppInfo")
@NoArgsConstructor
public class TppInfo {
    @Id
    @Column(name = "tpp_info_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tpp_info_generator")
    @SequenceGenerator(name = "tpp_info_generator", sequenceName = "tpp_info_id_seq")
    private Long id;

    @Column(name = "authorisation_number", nullable = false)
    @ApiModelProperty(value = "Authorisation number", required = true, example = "1234_authorisationNumber")
    private String authorisationNumber;

    @Column(name = "tpp_name", nullable = false)
    @ApiModelProperty(value = "Tpp name", required = true, example = "Tpp company")
    private String tppName;

    @ElementCollection
    @CollectionTable(name = "tpp_info_role", joinColumns = @JoinColumn(name = "tpp_info_id"))
    @Column(name = "tpp_role")
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Tpp roles", required = true, dataType = "array")
    private List<CmsTppRole> tppRoles;

    @Column(name = "authority_id", nullable = false)
    @ApiModelProperty(value = "National competent authority id", required = true, example = "authority id")
    private String authorityId;

    @Column(name = "authority_name", nullable = false)
    @ApiModelProperty(value = "National competent authority name", required = true, example = "authority name")
    private String authorityName;

    @Column(name = "country", nullable = false)
    @ApiModelProperty(value = "Country", required = true, example = "Germany")
    private String country;

    @Column(name = "organisation", nullable = false)
    @ApiModelProperty(value = "Organisation", required = true, example = "Organisation")
    private String organisation;

    @Column(name = "organisation_unit", nullable = false)
    @ApiModelProperty(value = "Organisation unit", required = true, example = "Organisation unit")
    private String organisationUnit;

    @Column(name = "city", nullable = false)
    @ApiModelProperty(value = "City", required = true, example = "Nuremberg")
    private String city;

    @Column(name = "state", nullable = false)
    @ApiModelProperty(value = "State", required = true, example = "Bayern")
    private String state;

    @Column(name = "redirect_uri", nullable = false)
    @ApiModelProperty(value = "Redirect URI", example = "Redirect URI")
    private String redirectUri;

    @Column(name = "nok_redirect_uri", nullable = false)
    @ApiModelProperty(value = "Nok redirect URI", example = "Nok redirect URI")
    private String nokRedirectUri;
}
