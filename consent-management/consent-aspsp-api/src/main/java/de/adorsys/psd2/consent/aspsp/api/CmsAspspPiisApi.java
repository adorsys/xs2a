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

package de.adorsys.psd2.consent.aspsp.api;

import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.api.ResponseData;
import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/piis/consents")
@Tag(name = CmsAspspApiTagName.ASPSP_PIIS_CONSENTS, description = "Controller for CMS-ASPSP-API providing access to PIIS consents")
public interface CmsAspspPiisApi {

    @PostMapping
    @Operation(description = "Creates new PIIS consent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    ResponseEntity<CreatePiisConsentResponse> createConsent(
        @RequestBody CreatePiisConsentRequest request,
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

    @GetMapping
    @Operation(description = "Returns a list of PIIS Consent objects by PSU ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseData<List<CmsPiisConsent>> getConsentsForPsu(
        @Parameter(description = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @Parameter(description = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @Parameter(description = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @Parameter(description = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @Parameter(description = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @DeleteMapping(path = "/{consent-id}")
    @Operation(description = "Terminates PIIS Consent object by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Boolean> terminateConsent(
        @Parameter(name = "consent-id",
            description = "The account consent identification assigned to the created account consent",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId,
        @Parameter(description = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);
}
