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

package de.adorsys.psd2.consent.api.authorisation;

import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Update authorization request", value = "UpdateAuthorisationRequest")
public class UpdateAuthorisationRequest {

    @ApiModelProperty(value = "Cms authorisation type", required = true)
    private AuthorisationType authorisationType;

    @ApiModelProperty(value = "Corresponding PSU", required = true)
    private PsuIdData psuData;

    @ApiModelProperty(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised' 'failed' 'exempted'.", required = true, example = "STARTED")
    private ScaStatus scaStatus;

    @ApiModelProperty(value = "An identification provided by the ASPSP for the later identification of the authentication method selection.")
    private String authenticationMethodId;

    @ApiModelProperty(value = "Password")
    private String password;

    @ApiModelProperty(value = "SCA authentication data")
    private String scaAuthenticationData;

    @ApiModelProperty(value = "SCA approach")
    private ScaApproach scaApproach;
}
