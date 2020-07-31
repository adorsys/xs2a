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

package de.adorsys.psd2.xs2a.core.authorisation;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel(description = "Authorisation object", value = "AuthorisationResponse")
@NoArgsConstructor
public class Authorisation {

    @ApiModelProperty(value = "ID of the Authorisation", required = true, example = "6dc3d5b3-5023-7848-3853-f7200a64e80d")
    private String authorisationId;

    @ApiModelProperty(value = "PSU identification data", required = true)
    private PsuIdData psuIdData;

    @ApiModelProperty(value = "An identification of the created account consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String parentId;

    @ApiModelProperty(value = "Authorisation type 'CONSENT', 'PIS_CREATION', 'PIS_CANCELLATION'.", required = true, example = "AIS")
    private AuthorisationType authorisationType;

    @ApiModelProperty(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised' 'failed' 'exempted'.", required = true, example = "STARTED")
    private ScaStatus scaStatus;

    @ApiModelProperty(value = "An identification provided by the ASPSP for the later identification of the authentication method selection.")
    private String authenticationMethodId;

    @ApiModelProperty(value = "Password")
    private String password;

    @ApiModelProperty(value = "SCA authentication data")
    private String scaAuthenticationData;

    @ApiModelProperty(value = "Chosen SCA approach")
    private ScaApproach chosenScaApproach;

    public Authorisation(String authorisationId, PsuIdData psuIdData, String parentId, AuthorisationType authorisationType, ScaStatus scaStatus) {
        this.authorisationId = authorisationId;
        this.psuIdData = psuIdData;
        this.parentId = parentId;
        this.authorisationType = authorisationType;
        this.scaStatus = scaStatus;
    }

}
