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

import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
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
@Api(value = "psu-api/v1/ais/consent", tags = {"PSU AIS Consents"})
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
        @SuppressWarnings("unused") @ApiParam(name = "consent-id", value = "The consent identifier", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("consent-id") String consentId,
        @ApiParam(name = "authorisation-id", value = "The authorisation identifier of the current authorisation session", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
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
    public ResponseEntity updateAuthorisationStatus(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised', 'failed', 'exempted'. These values might be extended by ASPSP by more values.", allowableValues = "RECEIVED, PSUIDENTIFIED, PSUAUTHENTICATED, SCAMETHODSELECTED,  STARTED,  FINALISED, FAILED, EXEMPTED")
        @PathVariable("status") String status,
        @ApiParam(name = "authorisation-id", value = "The consent authorisation identification assigned to the created authorisation.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        ScaStatus scaStatus = ScaStatus.fromValue(status);
        if (scaStatus == null) {
            return ResponseEntity.badRequest().build();
        }

        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        try {
            return cmsPsuAisService.updateAuthorisationStatus(psuIdData, consentId, authorisationId, scaStatus, instanceId)
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
    public ResponseEntity<Boolean> confirmConsent( // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1067
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @Deprecated @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @Deprecated @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @Deprecated@RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @Deprecated @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return new ResponseEntity<>(cmsPsuAisService.confirmConsent(consentId, instanceId), HttpStatus.OK);
    }

    @PutMapping(path = "/{consent-id}/reject-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to REJECTED")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> rejectConsent( // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1067
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @Deprecated @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @Deprecated @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @Deprecated @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @Deprecated @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return new ResponseEntity<>(cmsPsuAisService.rejectConsent(consentId, instanceId), HttpStatus.OK);
    }

    @GetMapping(path = "/consents")
    @ApiOperation(value = "Returns a list of AIS Consent objects by PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<CmsAisAccountConsent>> getConsentsForPsu(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return new ResponseEntity<>(cmsPsuAisService.getConsentsForPsu(psuIdData, instanceId), HttpStatus.OK);
    }

    @PutMapping(path = "/{consent-id}/revoke-consent")
    @ApiOperation(value = "Revokes AIS Consent object by its ID. Consent gets status Revoked by PSU.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> revokeConsent( // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1067
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @Deprecated @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @Deprecated @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @Deprecated @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @Deprecated @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return new ResponseEntity<>(cmsPsuAisService.revokeConsent(consentId, instanceId), HttpStatus.OK);
    }

    @GetMapping(path = "/redirect/{redirect-id}")
    @ApiOperation(value = "Gets consent response by redirect ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisConsentResponse.class),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)})
    public ResponseEntity getConsentIdByRedirectId(
        @ApiParam(name = "redirect-id", value = "The redirect identification assigned to the created consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("redirect-id") String redirectId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

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
    public ResponseEntity<CmsAisAccountConsent> getConsentByConsentId(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
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
        @ApiParam(name = "authorisation-id", value = "The authorisation identification.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorisation-id") String authorisationId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

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
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @RequestBody CmsAisConsentAccessRequest accountAccessRequest,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        boolean accessSaved = cmsPsuAisService.updateAccountAccessInConsent(consentId, accountAccessRequest, instanceId);

        if (accessSaved) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    private PsuIdData getPsuIdData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        return new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
    }

    @GetMapping(path = "/{consent-id}/authorisation/psus")
    @ApiOperation(value = "Returns list of info objects about psu data and authorisation scaStatuses")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisPsuDataAuthorisation.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<CmsAisPsuDataAuthorisation>> psuDataAuthorisations(
        @ApiParam(name = "consent-id", value = "The consent identification assigned to the created consent authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        return cmsPsuAisService.getPsuDataAuthorisations(consentId, instanceId)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

}
