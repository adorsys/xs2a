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

package de.adorsys.psd2.consent.web.xs2a.controller;


import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.ConsentStatusResponse;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.consent.web.xs2a.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/consent")
@Api(value = "api/v1/consent", tags = InternalCmsXs2aApiTagName.CONSENTS)
public class CmsConsentController {
    private final ConsentServiceEncrypted consentServiceEncrypted;

    @PostMapping
    @ApiOperation(value = "Create new consent")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = String.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 204, message = "No Content")})
    public ResponseEntity<Object> createConsent(@RequestBody CmsConsent request) {
        CmsResponse<CmsCreateConsentResponse> cmsResponse;

        try {
            cmsResponse = consentServiceEncrypted.createConsent(request);
        } catch (WrongChecksumException e) {
            return new ResponseEntity<>(CmsError.CHECKSUM_ERROR, HttpStatus.BAD_REQUEST);
        }

        if (cmsResponse.hasError()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(cmsResponse.getPayload(), HttpStatus.CREATED);
    }

    @GetMapping(path = "/{encrypted-consent-id}")
    @ApiOperation(value = "Read consent by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsConsent.class),
        @ApiResponse(code = 404, message = "Not found")})
    public ResponseEntity<CmsConsent> getConsentById(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId) {
        CmsResponse<CmsConsent> consentById = consentServiceEncrypted.getConsentById(encryptedConsentId);

        if (consentById.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(consentById.getPayload(), HttpStatus.OK);
    }

    @GetMapping(path = "/{encrypted-consent-id}/status")
    @ApiOperation(value = "Get consent status by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ConsentStatus.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<ConsentStatusResponse> getConsentStatusById(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId) {
        CmsResponse<ConsentStatus> consentStatusById = consentServiceEncrypted.getConsentStatusById(encryptedConsentId);

        if (consentStatusById.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new ConsentStatusResponse(consentStatusById.getPayload()), HttpStatus.OK);
    }

    @PutMapping(path = "/{encrypted-consent-id}/status/{status}")
    @ApiOperation(value = "Update consent status by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Object> updateConsentStatus(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @ApiParam(value = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.",
            example = "VALID",
            required = true)
        @PathVariable("status") String status) {
        CmsResponse<Boolean> response;

        try {
            response = consentServiceEncrypted.updateConsentStatusById(encryptedConsentId, ConsentStatus.valueOf(status));
        } catch (WrongChecksumException e) {
            return new ResponseEntity<>(CmsError.CHECKSUM_ERROR, HttpStatus.BAD_REQUEST);
        }

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(path = "/{encrypted-consent-id}/old-consents")
    @ApiOperation(value = "Find and terminate old consents for TPP and PSU by new consent ID")
    @ApiResponse(code = 204, message = "No Content")
    public ResponseEntity<Void> findAndTerminateOldConsentsByNewConsentId(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId) {
        consentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(encryptedConsentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/{encrypted-consent-id}/multilevel-sca")
    @ApiOperation(value = "Update requirement for multilevel SCA for consent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Object> updateMultilevelScaRequired(
        @ApiParam(name = "encrypted-consent-id", value = "Encrypted consent ID", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @ApiParam(name = "multilevel-sca", value = "Multilevel SCA.", example = "false")
        @RequestParam(value = "multilevel-sca", defaultValue = "false") boolean multilevelSca) {
        CmsResponse<Boolean> response;

        try {
            response = consentServiceEncrypted.updateMultilevelScaRequired(encryptedConsentId, multilevelSca);
        } catch (WrongChecksumException e) {
            return new ResponseEntity<>(CmsError.CHECKSUM_ERROR, HttpStatus.BAD_REQUEST);
        }

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
