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

package de.adorsys.psd2.consent.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class CmsTppInfo {
    @ApiModelProperty(value = "Authorisation number", required = true, example = "12345987")
    private String authorisationNumber;

    @ApiModelProperty(value = "Tpp name", required = true, example = "Tpp company")
    private String tppName;

    @ApiModelProperty(value = "Tpp roles", required = true, dataType = "array")
    private List<CmsTppRole> tppRoles;

    @ApiModelProperty(value = "National competent authority id", required = true, example = "authority id")
    private String authorityId;

    @ApiModelProperty(value = "National competent authority name", required = true, example = "authority name")
    private String authorityName;

    @ApiModelProperty(value = "Country", required = true, example = "Germany")
    private String country;

    @ApiModelProperty(value = "Organisation", required = true, example = "Organisation")
    private String organisation;

    @ApiModelProperty(value = "Organisation unit", required = true, example = "Organisation unit")
    private String organisationUnit;

    @ApiModelProperty(value = "City", required = true, example = "Nuremberg")
    private String city;

    @ApiModelProperty(value = "State", required = true, example = "Bayern")
    private String state;

    @ApiModelProperty(value = "Redirect URI", example = "Redirect URI")
    private String redirectUri;

    @ApiModelProperty(value = "Nok redirect URI", example = "Nok redirect URI")
    private String nokRedirectUri;
}
