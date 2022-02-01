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

package de.adorsys.psd2.consent.psu.api;

import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.consent.psu.api.config.CmsPsuApiTagName;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.adorsys.psd2.consent.psu.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "psu-api/v1/ais/consent")
@Api(value = "psu-api/v1/ais/consent", tags = CmsPsuApiTagName.PSU_AIS_CONSENTS)
public interface CmsPsuAisApi {

    @PutMapping(path = "/{consent-id}/authorisation/{authorisation-id}/psu-data")
    @ApiOperation(value = "Updates PSU Data in consent, based on the trusted information about PSU known to ASPSP (i.e. after authorisation).")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)
    })
    ResponseEntity<Object> updatePsuDataInConsent(
        @SuppressWarnings("unused")
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID, value = "The consent identifier", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @ApiParam(name = CmsConstant.PATH.AUTHORISATION_ID, value = "The authorisation identifier of the current authorisation session", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody PsuIdData psuIdData);

    @PutMapping(path = "/{consent-id}/authorisation/{authorisation-id}/status/{status}")
    @ApiOperation(value = "Updates a Status of AIS Consent Authorisation by its ID and PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)})
    @PsuHeadersDescription
    ResponseEntity<Object> updateAuthorisationStatus(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @ApiParam(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised', 'failed', 'exempted', 'unconfirmed'. These values might be extended by ASPSP by more values.",
            allowableValues = "RECEIVED, PSUIDENTIFIED, PSUAUTHENTICATED, SCAMETHODSELECTED,  STARTED,  FINALISED, FAILED, EXEMPTED, UNCONFIRMED",
            required = true)
        @PathVariable(CmsConstant.PATH.STATUS) String status,
        @ApiParam(name = CmsConstant.PATH.AUTHORISATION_ID,
            value = "The consent authorisation identification assigned to the created authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID, required = false) String psuId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID_TYPE, required = false) String psuIdType,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID, required = false) String psuCorporateId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, required = false) String psuCorporateIdType,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody(required = false) AuthenticationDataHolder authenticationDataHolder);

    @PutMapping(path = "/{consent-id}/confirm-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to VALID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<Boolean> confirmConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @PutMapping(path = "/{consent-id}/reject-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to REJECTED")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<Boolean> rejectConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @GetMapping(path = "/consents")
    @ApiOperation(value = "Returns a list of AIS Consent objects by PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    @PsuHeadersDescription
    ResponseEntity<List<CmsAisAccountConsent>> getConsentsForPsu(
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID, required = false) String psuId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID_TYPE, required = false) String psuIdType,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID, required = false) String psuCorporateId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, required = false) String psuCorporateIdType,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestParam(value = CmsConstant.QUERY.ADDITIONAL_TPP_INFO, required = false) String additionalTppInfo,
        @ApiParam(name = CmsConstant.QUERY.STATUS, value = "Consent statuses. ", example = "VALID,EXPIRED")
        @RequestParam(value = CmsConstant.QUERY.STATUS, required = false) List<String> status,
        @ApiParam(name = CmsConstant.QUERY.ACCOUNT_NUMBER, value = "Account numbers ", example = "DE2310010010123452343,DE2310010010123452344")
        @RequestParam(value = CmsConstant.QUERY.ACCOUNT_NUMBER, required = false) List<String> accountNumbers,
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, required = false) Integer pageIndex,
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, required = false) Integer itemsPerPage);

    @PutMapping(path = "/{consent-id}/revoke-consent")
    @ApiOperation(value = "Revokes AIS Consent object by its ID. Consent gets status Revoked by PSU.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<Boolean> revokeConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @PutMapping(path = "/{consent-id}/authorise-partially-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to PARTIALLY_AUTHORISED.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    @PsuHeadersDescription
    ResponseEntity<Boolean> authorisePartiallyConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @GetMapping(path = "/redirect/{redirect-id}")
    @ApiOperation(value = "Gets consent response by redirect ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisConsentResponse.class),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)})
    ResponseEntity<CmsAisConsentResponse> getConsentIdByRedirectId(
        @ApiParam(name = CmsConstant.PATH.REDIRECT_ID, value = "The redirect identification assigned to the created consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable(CmsConstant.PATH.REDIRECT_ID) String redirectId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "Returns AIS Consent object by its ID.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisAccountConsent.class),
        @ApiResponse(code = 404, message = "Not Found")})
    @PsuHeadersDescription
    ResponseEntity<CmsAisAccountConsent> getConsentByConsentId(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID, required = false) String psuId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID_TYPE, required = false) String psuIdType,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID, required = false) String psuCorporateId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, required = false) String psuCorporateIdType,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @GetMapping(path = "authorisation/{authorisation-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPsuAuthorisation.class),
        @ApiResponse(code = 400, message = "Bad request")})
    ResponseEntity<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(
        @ApiParam(name = CmsConstant.PATH.AUTHORISATION_ID,
            value = "The authorisation identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @PutMapping(path = "/{consent-id}/save-access")
    @ApiOperation(value = "Stores list of accounts with their identifiers in AIS Consent object by its ID. Consent should not be revoked, cancelled or expired.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", reference = "Access saved"),
        @ApiResponse(code = 404, message = "Not Found", reference = "Consent not found or not active")})
    ResponseEntity<Void> putAccountAccessInConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestBody CmsAisConsentAccessRequest accountAccessRequest,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @GetMapping(path = "/{consent-id}/authorisation/psus")
    @ApiOperation(value = "Returns list of info objects about psu data and authorisation scaStatuses")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisPsuDataAuthorisation.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<List<CmsAisPsuDataAuthorisation>> psuDataAuthorisations(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The consent identification assigned to the created consent authorization.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, required = false) Integer pageIndex,
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, required = false) Integer itemsPerPage);
}
