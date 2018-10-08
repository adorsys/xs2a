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

package de.adorsys.psd2.consent.server.domain.payment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity(name = "pis_tpp_info")
@ApiModel(description = "Tpp info", value = "PisTppInfo")
@NoArgsConstructor
public class PisTppInfo {
    @Id
    @Column(name = "tpp_info_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_tpp_info_generator")
    @SequenceGenerator(name = "pis_tpp_info_generator", sequenceName = "pis_tpp_info_id_seq")
    private Long id;

    @Column(name = "registration_number", nullable = false)
    @ApiModelProperty(value = "Registration number", required = true, example = "1234_registrationNumber")
    private String registrationNumber;

    @Column(name = "tpp_name", nullable = false)
    @ApiModelProperty(value = "Tpp name", required = true, example = "Tpp company")
    private String tppName;

    @Column(name = "tpp_role", nullable = false)
    @ApiModelProperty(value = "Tpp role", required = true, example = "Tpp role")
    private String tppRole;

    @Column(name = "national_competent_authority", nullable = false)
    @ApiModelProperty(value = "National competent authority", required = true, example = "National competent authority")
    private String nationalCompetentAuthority;

    @Column(name = "redirect_uri", nullable = false)
    @ApiModelProperty(value = "Redirect URI", required = true, example = "Redirect URI")
    private String redirectUri;

    @Column(name = "nok_redirect_uri", nullable = false)
    @ApiModelProperty(value = "Nok redirect URI", required = true, example = "Nok redirect URI")
    private String nokRedirectUri;
}
