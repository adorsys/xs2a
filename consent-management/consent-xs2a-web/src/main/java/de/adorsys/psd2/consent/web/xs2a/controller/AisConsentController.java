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

package de.adorsys.psd2.consent.web.xs2a.controller;


import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AccountServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//TODO Rename consentId to encryptedConsentId https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/705
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/ais/consent")
@Api(value = "api/v1/ais/consent", tags = "AIS, Consents", description = "Provides access to consent management system for AIS")
public class AisConsentController {
    private final AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;
    private final AisConsentServiceEncrypted aisConsentService;
    private final AccountServiceEncrypted accountServiceEncrypted;

    @PostMapping(path = "/")
    @ApiOperation(value = "Create consent for given psu id and accesses.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = String.class),
        @ApiResponse(code = 204, message = "No Content")})
    public ResponseEntity<CreateAisConsentResponse> createConsent(@RequestBody CreateAisConsentRequest request) {
        CmsResponse<CreateAisConsentResponse> cmsResponse = aisConsentService.createConsent(request);

        if (cmsResponse.hasError()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(cmsResponse.getPayload(), HttpStatus.CREATED);
    }

    @PostMapping(path = "/action")
    @ApiOperation(value = "Save information about uses of consent")
    public ResponseEntity<Void> saveConsentActionLog(@RequestBody AisConsentActionRequest request) {
        aisConsentService.checkConsentAndSaveActionLog(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "Read account consent by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AisAccountConsent.class),
        @ApiResponse(code = 204, message = "No Content")})
    public ResponseEntity<AisAccountConsent> getConsentById(
        @ApiParam(name = "consent-id",
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId) {
        CmsResponse<AisAccountConsent> consentById = aisConsentService.getAisAccountConsentById(consentId);

        if (consentById.hasError()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(consentById.getPayload(), HttpStatus.OK);
    }

    @PutMapping(path = "/{consent-id}/access")
    @ApiOperation(value = "Update AccountAccess in the consent identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<UpdateAisConsentResponse> updateAccountAccess(
        @ApiParam(name = "consent-id",
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId,
        @RequestBody AisAccountAccessInfo request) {
        CmsResponse<AisAccountConsent> response = aisConsentService.updateAspspAccountAccessWithResponse(consentId, request);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new UpdateAisConsentResponse(response.getPayload()), HttpStatus.OK);
    }

    @GetMapping(path = "/{consent-id}/status")
    @ApiOperation(value = "Can check the status of an account information consent resource.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ConsentStatus.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<AisConsentStatusResponse> getConsentStatusById(
        @ApiParam(name = "consent-id",
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId) {
        CmsResponse<ConsentStatus> consentStatusById = aisConsentService.getConsentStatusById(consentId);

        if (consentStatusById.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new AisConsentStatusResponse(consentStatusById.getPayload()), HttpStatus.OK);
    }

    @PutMapping(path = "/{consent-id}/status/{status}")
    @ApiOperation(value = "Update consent status in the consent identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Void> updateConsentStatus(
        @ApiParam(name = "consent-id",
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.",
            example = "VALID",
            required = true)
        @PathVariable("status") String status) {
        CmsResponse<Boolean> response = aisConsentService.updateConsentStatusById(consentId, ConsentStatus.valueOf(status));
        if (response.isSuccessful() && response.getPayload()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(path = "/{consent-id}/old-consents")
    @ApiOperation(value = "Find old consents for current TPP and PSU and terminates them")
    @ApiResponse(code = 204, message = "No Content")
    public ResponseEntity<Void> findAndTerminateOldConsentsByNewConsentId(
        @ApiParam(name = "consent-id",
            value = "The account consent identification assigned to the new account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId) {
        aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{consent-id}/authorizations")
    @ApiOperation(value = "Create consent authorization for given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CreateAisConsentAuthorizationResponse> createConsentAuthorization(
        @ApiParam(name = "consent-id",
            value = "The consent identification assigned to the created consent authorization.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId,
        @RequestBody AisConsentAuthorizationRequest consentAuthorization) {
        CmsResponse<CreateAisConsentAuthorizationResponse> response = aisConsentAuthorisationServiceEncrypted.createAuthorizationWithResponse(consentId, consentAuthorization);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.CREATED);
    }

    @PutMapping(path = "/authorizations/{authorization-id}")
    @ApiOperation(value = "Update consent authorization.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Void> updateConsentAuthorization(
        @ApiParam(name = "authorization-id",
            value = "The consent authorization identification assigned to the created authorization.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorization-id") String authorizationId,
        @ApiParam(value = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.", example = "VALID")
        @RequestBody AisConsentAuthorizationRequest consentAuthorization) {
        CmsResponse<Boolean> response = aisConsentAuthorisationServiceEncrypted.updateConsentAuthorization(authorizationId, consentAuthorization);

        if (response.isSuccessful() && response.getPayload()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping(path = "/authorisations/{authorisation-id}/status/{status}")
    @ApiOperation(value = "Update consent authorisation status.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Void> updateConsentAuthorisationStatus(
        @ApiParam(name = "authorisation-id",
            value = "The consent authorisation identification assigned to the created authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(value = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.",
            example = "VALID",
            required = true)
        @PathVariable("status") String scaStatus) {
        CmsResponse<Boolean> response = aisConsentAuthorisationServiceEncrypted.updateConsentAuthorisationStatus(authorisationId, ScaStatus.fromValue(scaStatus));

        if (response.isSuccessful() && response.getPayload()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(path = "/{consent-id}/authorizations/{authorization-id}")
    @ApiOperation(value = "Getting consent authorization.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<AisConsentAuthorizationResponse> getConsentAuthorization(
        @ApiParam(name = "consent-id",
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId,
        @ApiParam(name = "authorization-id",
            value = "The consent authorization identification assigned to the created authorization.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorization-id") String authorizationId) {
        CmsResponse<AisConsentAuthorizationResponse> response = aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(authorizationId, consentId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @GetMapping(path = "/{consent-id}/authorisations/{authorisation-id}/status")
    @ApiOperation(value = "Gets SCA status of consent authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<ScaStatus> getConsentAuthorizationScaStatus(
        @ApiParam(name = "consent-id",
            value = "Account consent identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId,
        @ApiParam(name = "authorisation-id",
            value = "Consent authorisation identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId) {
        CmsResponse<ScaStatus> response = aisConsentAuthorisationServiceEncrypted.getAuthorisationScaStatus(consentId, authorisationId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @GetMapping(path = "/{consent-id}/authorisations")
    @ApiOperation(value = "Gets list of consent authorisation IDs by consent ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<String>> getConsentAuthorisation(
        @ApiParam(name = "consent-id",
            value = "The account consent identification assigned to the created account consent.",
            example = "vOHy6fj2f5IgxHk-kTlhw6sZdTXbRE3bWsu2obq54beYOChP5NvRmfh06nrwumc2R01HygQenchEcdGOlU-U0A==_=_iR74m2PdNyE",
            required = true)
        @PathVariable("consent-id") String consentId) {
        CmsResponse<List<String>> response = aisConsentAuthorisationServiceEncrypted.getAuthorisationsByConsentId(consentId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @GetMapping(path = "/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}")
    @ApiOperation(value = "Checks if requested authentication method is decoupled")
    @ApiResponse(code = 200, message = "OK")
    public ResponseEntity<Boolean> isAuthenticationMethodDecoupled(
        @ApiParam(name = "authorisation-id",
            value = "Consent authorisation identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(name = "authentication-method-id",
            value = "Authentication method identification",
            example = "sms",
            required = true)
        @PathVariable("authentication-method-id") String authenticationMethodId) {
        CmsResponse<Boolean> response = aisConsentAuthorisationServiceEncrypted.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @PostMapping(path = "/authorisations/{authorisation-id}/authentication-methods")
    @ApiOperation(value = "Saves authentication methods in authorisation")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Void> saveAuthenticationMethods(
        @ApiParam(name = "authorisation-id",
            value = "The consent authorisation identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestBody List<CmsScaMethod> methods) {
        CmsResponse<Boolean> response = aisConsentAuthorisationServiceEncrypted.saveAuthenticationMethods(authorisationId, methods);

        if (response.isSuccessful() && response.getPayload()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping(path = "/authorisations/{authorisation-id}/sca-approach/{sca-approach}")
    @ApiOperation(value = "Updates AIS SCA approach in authorisation")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> updateScaApproach(
        @ApiParam(name = "authorisation-id",
            value = "The consent authorisation identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(name = "sca-approach",
            value = "Chosen SCA approach.",
            example = "REDIRECT",
            required = true)
        @PathVariable("sca-approach") ScaApproach scaApproach) {
        CmsResponse<Boolean> response = aisConsentAuthorisationServiceEncrypted.updateScaApproach(authorisationId, scaApproach);

        if (response.isSuccessful() && response.getPayload()) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping(path = "/{consent-id}/multilevel-sca")
    @ApiOperation(value = "Updates multilevel SCA in consent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> updateMultilevelScaRequired(
        @ApiParam(name = "consent-id", value = "The consent identification.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("consent-id") String consentId,
        @ApiParam(name = "multilevel-sca", value = "Multilevel SCA.", example = "false")
        @RequestParam(value = "multilevel-sca", defaultValue = "false") boolean multilevelSca) {
        CmsResponse<Boolean> response = aisConsentService.updateMultilevelScaRequired(consentId, multilevelSca);

        if (response.isSuccessful() && response.getPayload()) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(path = "/authorisations/{authorisation-id}/sca-approach")
    @ApiOperation(value = "Gets SCA approach of the consent authorisation by its ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<AuthorisationScaApproachResponse> getAuthorisationScaApproach(
        @ApiParam(name = "authorisation-id",
            value = "The consent authorisation identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId) {
        CmsResponse<AuthorisationScaApproachResponse> response = aisConsentAuthorisationServiceEncrypted.getAuthorisationScaApproach(authorisationId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @PutMapping(path = "/{consent-id}/{resource-id}")
    @ApiOperation(value = "Saves number of transactions for a definite account of the definite consent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> saveNumberOfTransactions(
        @ApiParam(name = "consent-id",
            value = "The consent identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("consent-id") String consentId,
        @PathVariable("resource-id") String resourceId,
        @RequestBody Integer numberOfTransactions) {
        return accountServiceEncrypted.saveNumberOfTransactions(consentId, resourceId, numberOfTransactions)
                   ? new ResponseEntity<>(true, HttpStatus.OK)
                   : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
