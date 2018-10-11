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

package de.adorsys.aspsp.aspspmockserver.domain.ais;

import de.adorsys.aspsp.aspspmockserver.domain.ScaStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "AIS consent authorization", value = "AisConsentAuthorization")
public class AisConsentAuthorizationResponse {

    @ApiModelProperty(value = "ID of the Authorization", required = true, example = "6dc3d5b3-5023-7848-3853-f7200a64e80d")
    private String authorizationId;

    @ApiModelProperty(value = "ID of the corresponding PSU", required = true, example = "32aad578-58a6-4d5d-8b0c-45546dd88f07")
    private String psuId;

    @ApiModelProperty(value = "An identification of the created account consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String consentId;

    @ApiModelProperty(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised' 'failed' 'exempted'.", required = true, example = "STARTED")
    private ScaStatus scaStatus;

    @ApiModelProperty(value = "An identification provided by the ASPSP for the later identification of the authentication method selection.")
    private String authenticationMethodId;

    @ApiModelProperty(value = "Password")
    private String password;

    @ApiModelProperty(value = "SCA authentication data")
    private String scaAuthenticationData;
}
