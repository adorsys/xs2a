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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.ConsentStatusResponse;
import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "api/v1/consent")
@Api(value = "api/v1/consent", tags = InternalCmsXs2aApiTagName.CONSENTS)
public interface CmsConsentApi {

    @PostMapping
    @ApiOperation(value = "Create new consent")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = String.class),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 204, message = "No Content")})
    ResponseEntity<Object> createConsent(@RequestBody CmsConsent request);

    @GetMapping(path = "/{encrypted-consent-id}")
    @ApiOperation(value = "Read consent by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsConsent.class),
        @ApiResponse(code = 404, message = "Not found")})
    ResponseEntity<CmsConsent> getConsentById(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId);

    @GetMapping(path = "/{encrypted-consent-id}/status")
    @ApiOperation(value = "Get consent status by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ConsentStatus.class),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<ConsentStatusResponse> getConsentStatusById(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId);

    @PutMapping(path = "/{encrypted-consent-id}/status/{status}")
    @ApiOperation(value = "Update consent status by ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<Object> updateConsentStatus(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @ApiParam(value = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.",
            example = "VALID",
            required = true)
        @PathVariable("status") String status);

    @DeleteMapping(path = "/{encrypted-consent-id}/old-consents")
    @ApiOperation(value = "Find and terminate old consents for TPP and PSU by new consent ID")
    @ApiResponse(code = 204, message = "No Content")
    ResponseEntity<Void> findAndTerminateOldConsentsByNewConsentId(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId);

    @PutMapping(path = "/{encrypted-consent-id}/old-consents")
    @ApiOperation(value = "Find and terminate old consents for TPP and PSU by new consent ID")
    @ApiResponse(code = 204, message = "No Content")
    ResponseEntity<Void> findAndTerminateOldConsents(
        @ApiParam(name = "encrypted-consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @RequestBody TerminateOldConsentsRequest cmsTerminateConsentsRequest);

    @PutMapping(path = "/{encrypted-consent-id}/multilevel-sca")
    @ApiOperation(value = "Update requirement for multilevel SCA for consent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<Object> updateMultilevelScaRequired(
        @ApiParam(name = "encrypted-consent-id", value = "Encrypted consent ID", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @ApiParam(name = "multilevel-sca", value = "Multilevel SCA.", example = "false")
        @RequestParam(value = "multilevel-sca", defaultValue = "false") boolean multilevelSca);
}
