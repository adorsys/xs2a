/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.core.authorisation;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "Authorisation object")
@NoArgsConstructor
public class Authorisation {

    @Schema(description = "ID of the Authorisation", required = true, example = "6dc3d5b3-5023-7848-3853-f7200a64e80d")
    private String authorisationId;

    @Schema(description = "PSU identification data", required = true)
    private PsuIdData psuIdData;

    @Schema(description = "An identification of the created account consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String parentId;

    @Schema(description = "Authorisation type 'CONSENT', 'PIS_CREATION', 'PIS_CANCELLATION'.", required = true, example = "AIS")
    private AuthorisationType authorisationType;

    @Schema(description = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised' 'failed' 'exempted'.", required = true, example = "STARTED")
    private ScaStatus scaStatus;

    @Schema(description = "An identification provided by the ASPSP for the later identification of the authentication method selection.")
    private String authenticationMethodId;

    @Schema(description = "Password")
    private String password;

    @Schema(description = "SCA authentication data")
    private String scaAuthenticationData;

    @Schema(description = "Chosen SCA approach")
    private ScaApproach chosenScaApproach;

    public Authorisation(String authorisationId, PsuIdData psuIdData, String parentId, AuthorisationType authorisationType, ScaStatus scaStatus) {
        this.authorisationId = authorisationId;
        this.psuIdData = psuIdData;
        this.parentId = parentId;
        this.authorisationType = authorisationType;
        this.scaStatus = scaStatus;
    }

}
