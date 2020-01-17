/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.consent.web.psu.PsuHeadersDescription;
import de.adorsys.psd2.consent.web.psu.config.CmsPsuApiTagName;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "psu-api/v1/ais/consent")
@Api(value = "psu-api/v1/ais/consent", tags = CmsPsuApiTagName.PSU_AIS_CONSENTS)
public class CmsPsuAisController {
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    private final CmsPsuAisService cmsPsuAisService;

    @PutMapping(path = "/{consent-id}/authorisation/{authorisation-id}/psu-data")
    @ApiOperation(value = "Updates PSU Data in consent, based on the trusted information about PSU known to ASPSP (i.e. after authorisation).")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)
    })
    public ResponseEntity updatePsuDataInConsent(
        @SuppressWarnings("unused") @ApiParam(name = CmsConstant.PATH.CONSENT_ID, value = "The consent identifier", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @ApiParam(name = CmsConstant.PATH.AUTHORISATION_ID, value = "The authorisation identifier of the current authorisation session", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody PsuIdData psuIdData) {

        try {
            return cmsPsuAisService.updatePsuDataInConsent(psuIdData, authorisationId, instanceId)
                       ? ResponseEntity.ok().build()
                       : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            return new ResponseEntity<>(new CmsAisConsentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @PutMapping(path = "/{consent-id}/authorisation/{authorisation-id}/status/{status}")
    @ApiOperation(value = "Updates a Status of AIS Consent Authorisation by its ID and PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)})
    @PsuHeadersDescription
    public ResponseEntity updateAuthorisationStatus(
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
        @RequestBody(required = false) AuthenticationDataHolder authenticationDataHolder) {

        ScaStatus scaStatus = ScaStatus.fromValue(status);
        if (scaStatus == null) {
            return ResponseEntity.badRequest().build();
        }

        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        try {
            return cmsPsuAisService.updateAuthorisationStatus(psuIdData, consentId, authorisationId, scaStatus, instanceId, authenticationDataHolder)
                       ? ResponseEntity.ok().build()
                       : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            return new ResponseEntity<>(new CmsAisConsentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @PutMapping(path = "/{consent-id}/confirm-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to VALID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> confirmConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return new ResponseEntity<>(cmsPsuAisService.confirmConsent(consentId, instanceId), HttpStatus.OK);
    }

    @PutMapping(path = "/{consent-id}/reject-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to REJECTED")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> rejectConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return new ResponseEntity<>(cmsPsuAisService.rejectConsent(consentId, instanceId), HttpStatus.OK);
    }

    @GetMapping(path = "/consents")
    @ApiOperation(value = "Returns a list of AIS Consent objects by PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    @PsuHeadersDescription
    public ResponseEntity<List<CmsAisAccountConsent>> getConsentsForPsu(
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID, required = false) String psuId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID_TYPE, required = false) String psuIdType,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID, required = false) String psuCorporateId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, required = false) String psuCorporateIdType,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return new ResponseEntity<>(cmsPsuAisService.getConsentsForPsu(psuIdData, instanceId), HttpStatus.OK);
    }

    @PutMapping(path = "/{consent-id}/revoke-consent")
    @ApiOperation(value = "Revokes AIS Consent object by its ID. Consent gets status Revoked by PSU.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> revokeConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return new ResponseEntity<>(cmsPsuAisService.revokeConsent(consentId, instanceId), HttpStatus.OK);
    }

    @PutMapping(path = "/{consent-id}/authorise-partially-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to PARTIALLY_AUTHORISED.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    @PsuHeadersDescription
    public ResponseEntity<Boolean> authorisePartiallyConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return new ResponseEntity<>(cmsPsuAisService.authorisePartiallyConsent(consentId, instanceId), HttpStatus.OK);
    }

    @GetMapping(path = "/redirect/{redirect-id}")
    @ApiOperation(value = "Gets consent response by redirect ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisConsentResponse.class),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)})
    public ResponseEntity<CmsAisConsentResponse> getConsentIdByRedirectId(
        @ApiParam(name = CmsConstant.PATH.REDIRECT_ID, value = "The redirect identification assigned to the created consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable(CmsConstant.PATH.REDIRECT_ID) String redirectId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        Optional<CmsAisConsentResponse> response;
        try {
            response = cmsPsuAisService.checkRedirectAndGetConsent(redirectId, instanceId);

            if (!response.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            CmsAisConsentResponse cmsAisConsentResponse = response.get();
            return new ResponseEntity<>(cmsAisConsentResponse, HttpStatus.OK);
        } catch (RedirectUrlIsExpiredException e) {
            return new ResponseEntity<>(new CmsAisConsentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "Returns AIS Consent object by its ID.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AisAccountConsent.class),
        @ApiResponse(code = 404, message = "Not Found")})
    @PsuHeadersDescription
    public ResponseEntity<CmsAisAccountConsent> getConsentByConsentId(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID, required = false) String psuId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_ID_TYPE, required = false) String psuIdType,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID, required = false) String psuCorporateId,
        @RequestHeader(value = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, required = false) String psuCorporateIdType,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return cmsPsuAisService.getConsent(psuIdData, consentId, instanceId)
                   .map(aisAccountConsent -> new ResponseEntity<>(aisAccountConsent, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "authorisation/{authorisation-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPsuAuthorisation.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(
        @ApiParam(name = CmsConstant.PATH.AUTHORISATION_ID,
            value = "The authorisation identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.AUTHORISATION_ID) String authorisationId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        return cmsPsuAisService.getAuthorisationByAuthorisationId(authorisationId, instanceId)
                   .map(payment -> new ResponseEntity<>(payment, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping(path = "/{consent-id}/save-access")
    @ApiOperation(value = "Stores list of accounts with their identifiers in AIS Consent object by its ID. Consent should not be revoked, cancelled or expired.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", reference = "Access saved"),
        @ApiResponse(code = 404, message = "Not Found", reference = "Consent not found or not active")})
    public ResponseEntity<Void> putAccountAccessInConsent(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestBody CmsAisConsentAccessRequest accountAccessRequest,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        boolean accessSaved = cmsPsuAisService.updateAccountAccessInConsent(consentId, accountAccessRequest, instanceId);

        if (accessSaved) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    private PsuIdData getPsuIdData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        return new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
    }

    @GetMapping(path = "/{consent-id}/authorisation/psus")
    @ApiOperation(value = "Returns list of info objects about psu data and authorisation scaStatuses")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisPsuDataAuthorisation.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<CmsAisPsuDataAuthorisation>> psuDataAuthorisations(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The consent identification assigned to the created consent authorization.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        return cmsPsuAisService.getPsuDataAuthorisations(consentId, instanceId)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

}
