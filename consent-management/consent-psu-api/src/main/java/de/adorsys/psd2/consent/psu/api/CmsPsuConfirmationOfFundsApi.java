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

package de.adorsys.psd2.consent.psu.api;

import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsResponse;
import de.adorsys.psd2.consent.psu.api.config.CmsPsuApiTagName;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.adorsys.psd2.consent.psu.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "psu-api/v2/piis/consent")
@Api(value = "psu-api/v2/piis/consent", tags = CmsPsuApiTagName.CONFIRMATION_OF_FUNDS)
public interface CmsPsuConfirmationOfFundsApi {

    @PutMapping(path = "/{consent-id}/authorisation/{authorisation-id}/status/{status}")
    @ApiOperation(value = "Updates a Status of Confirmation of Funds Consent Authorisation by its ID and PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsConfirmationOfFundsResponse.class)})
    @PsuHeadersDescription
    ResponseEntity<Object> updateAuthorisationStatus(
        @ApiParam(name = CmsConstant.PATH.CONSENT_ID,
            value = "The confirmation of funds consent identification assigned to the created confirmation of funds consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(CmsConstant.PATH.CONSENT_ID) String consentId,
        @ApiParam(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised', 'failed', 'exempted', 'unconfirmed'. These values might be extended by ASPSP by more values.",
            allowableValues = "RECEIVED, PSUIDENTIFIED, PSUAUTHENTICATED, SCAMETHODSELECTED,  STARTED,  FINALISED, FAILED, EXEMPTED, UNCONFIRMED",
            required = true)
        @PathVariable(CmsConstant.PATH.STATUS) String status,
        @ApiParam(name = CmsConstant.PATH.AUTHORISATION_ID,
            value = "The confirmation of funds consent authorisation identification assigned to the created authorisation.",
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
    @ApiOperation(value = "Gets consent response by redirect ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsAisConsentResponse.class),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsAisConsentResponse.class)})
    ResponseEntity<CmsConfirmationOfFundsResponse> getConsentIdByRedirectId(
        @ApiParam(name = CmsConstant.PATH.REDIRECT_ID, value = "The redirect identification assigned to the created consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable(CmsConstant.PATH.REDIRECT_ID) String redirectId,
        @RequestHeader(value = CmsConstant.HEADERS.INSTANCE_ID, required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @PutMapping(path = "/{consent-id}/authorisation/{authorisation-id}/psu-data")
    @ApiOperation(value = "Updates PSU Data in Confirmation of Funds Consent, based on the trusted information about PSU known to ASPSP (i.e. after authorisation).")
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

}
