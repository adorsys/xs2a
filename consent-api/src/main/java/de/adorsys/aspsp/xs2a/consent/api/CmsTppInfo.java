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

package de.adorsys.aspsp.xs2a.consent.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CmsTppInfo {
    @ApiModelProperty(value = "Registration number", required = true, example = "1234_registrationNumber")
    private String registrationNumber;

    @ApiModelProperty(value = "Tpp name", required = true, example = "Tpp company")
    private String tppName;

    @ApiModelProperty(value = "Tpp role", required = true, example = "Tpp role")
    private String tppRole;

    @ApiModelProperty(value = "National competent authority", required = true, example = "National competent authority")
    private String nationalCompetentAuthority;

    @ApiModelProperty(value = "Redirect URI", required = true, example = "Redirect URI")
    private String redirectUri;

    @ApiModelProperty(value = "Nok redirect URI", required = true, example = "Nok redirect URI")
    private String nokRedirectUri;
}
