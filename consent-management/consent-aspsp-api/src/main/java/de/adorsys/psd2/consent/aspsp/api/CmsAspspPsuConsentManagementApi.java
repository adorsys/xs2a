/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.aspsp.api;

import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/psu/consent")
@Tag(name = CmsAspspApiTagName.ASPSP_PSU_CONSENT_MANAGEMENT, description = "CMS-ASPSP PSU Consent Management Controller")
public interface CmsAspspPsuConsentManagementApi {

    @DeleteMapping(path = "/all")
    @Operation(description = "Close all PIIS and AIS consents for PSU or account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    ResponseEntity<Boolean> closeAllConsents(
        @Parameter(description = "Bank specific account identifier", example = "11111-99999")
        @RequestHeader(value = "account-id", required = false) String aspspAccountId,
        @Parameter(description = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session.")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @Parameter(description = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @Parameter(description = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);
}
