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

package de.adorsys.psd2.consent.psu.api;

import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsConsent;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsResponse;
import de.adorsys.psd2.consent.psu.api.config.CmsPsuApiTagName;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.adorsys.psd2.consent.psu.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "psu-api/v2/piis/consent")
@Tag(name = CmsPsuApiTagName.CONFIRMATION_OF_FUNDS, description = "CMS-PSU Confirmation Of Funds Controller")
public interface CmsPsuConfirmationOfFundsApi {

    @PutMapping(path = "/{consent-id}/authorisation/{authorisation-id}/status/{status}")
    @Operation(description = "Updates a Status of Confirmation of Funds Consent Authorisation by its ID and PSU ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "408", description = "Request Timeout", content = @Content(schema = @Schema(implementation = CmsConfirmationOfFundsResponse.class)))})
    @PsuHeadersDescription
    ResponseEntity<Object> updateAuthorisationStatus(
        @Parameter(name = CmsConstant.PATH.CONSENT_ID,
            description = "The confirmation of funds consent identification assigned to the created confirmation of funds consent",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @Schema(description = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised', 'failed', 'exempted', 'unconfirmed'. These values might be extended by ASPSP by more values.",
            allowableValues = "RECEIVED, PSUIDENTIFIED, PSUAUTHENTICATED, SCAMETHODSELECTED,  STARTED,  FINALISED, FAILED, EXEMPTED, UNCONFIRMED",
            required = true)
        @PathVariable(CmsConstant.PATH.STATUS) String status,
        @Parameter(name = CmsConstant.PATH.AUTHORISATION_ID,
            description = "The confirmation of funds consent authorisation identification assigned to the created authorisation",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID, required = false) String psuId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID_TYPE, required = false) String psuIdType,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID, required = false) String psuCorporateId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, required = false) String psuCorporateIdType,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody(required = false) AuthenticationDataHolder authenticationDataHolder);

    @GetMapping(path = "/redirect/{redirect-id}")
    @Operation(description = "Gets consent response by redirect ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CmsAisConsentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found"),
        @ApiResponse(responseCode = "408", description = "Request Timeout", content = @Content(schema = @Schema(implementation = CmsAisConsentResponse.class)))})
    ResponseEntity<CmsConfirmationOfFundsResponse> getConsentByRedirectId(
        @Parameter(name = CmsConstant.PATH.REDIRECT_ID, description = "The redirect identification assigned to the created consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable(CmsConstant.PATH.REDIRECT_ID) String redirectId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @PutMapping(path = "/{consent-id}/authorisation/{authorisation-id}/psu-data")
    @Operation(description = "Updates PSU Data in Confirmation of Funds Consent, based on the trusted information about PSU known to ASPSP (i.e. after authorisation).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "408", description = "Request Timeout", content = @Content(schema = @Schema(implementation = CmsAisConsentResponse.class)))})
    ResponseEntity<Object> updatePsuDataInConsent(
        @SuppressWarnings("unused")
        @Parameter(name = CmsConstant.PATH.CONSENT_ID, description = "The consent identifier", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @Parameter(name = CmsConstant.PATH.AUTHORISATION_ID, description = "The authorisation identifier of the current authorisation session", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody PsuIdData psuIdData);


    @GetMapping(path = "authorisation/{authorisation-id}")
    @Operation(description = "Get Confirmation of Funds Consent Authorisation by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CmsPsuAuthorisation.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")})
    ResponseEntity<CmsPsuConfirmationOfFundsAuthorisation> getAuthorisationByAuthorisationId(
        @Parameter(name = CmsConstant.PATH.AUTHORISATION_ID,
            description = "The authorisation identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @PutMapping(path = "/{consent-id}/status/{status}")
    @Operation(description = "Updates a status of Confirmation of Funds Consent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")})
    ResponseEntity<Void> updateConsentStatus(
        @Parameter(name = CmsConstant.PATH.CONSENT_ID,
            description = "The confirmation of funds consent identification assigned to the created confirmation of funds consent",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @Schema(description = "The following code values are permitted 'RECEIVED', 'REJECTED', 'VALID', 'REVOKED_BY_PSU', 'EXPIRED', 'TERMINATED_BY_TPP', 'TERMINATED_BY_ASPSP', 'PARTIALLY_AUTHORISED'. These values might be extended by ASPSP by more values.",
            allowableValues = "RECEIVED, REJECTED, VALID, REVOKED_BY_PSU, EXPIRED, TERMINATED_BY_TPP, TERMINATED_BY_ASPSP, PARTIALLY_AUTHORISED",
            required = true)
        @PathVariable(CmsConstant.PATH.STATUS) String status,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);


    @GetMapping(path = "/{consent-id}")
    @Operation(description = "Returns confirmation of funds consent object by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CmsAisAccountConsent.class))),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    @PsuHeadersDescription
    ResponseEntity<CmsConfirmationOfFundsConsent> getConsentByConsentId(
        @Parameter(name = CmsConstant.PATH.CONSENT_ID,
            description = "The consent identification assigned to the created confirmation of funds consent",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID, required = false) String psuId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID_TYPE, required = false) String psuIdType,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID, required = false) String psuCorporateId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, required = false) String psuCorporateIdType,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);
}
