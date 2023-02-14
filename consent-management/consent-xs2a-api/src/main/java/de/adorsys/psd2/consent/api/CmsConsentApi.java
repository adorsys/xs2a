/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "api/v1/consent")
@Tag(name = InternalCmsXs2aApiTagName.CONSENTS, description = "Provides access to consent management system for common consent endpoints")
public interface CmsConsentApi {

    @PostMapping
    @Operation(description = "Create new consent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Checksum verification failed"),
        @ApiResponse(responseCode = "204", description = "No Content")})
    ResponseEntity<Object> createConsent(@RequestBody CmsConsent request);

    @GetMapping(path = "/{encrypted-consent-id}")
    @Operation(description = "Read consent by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CmsConsent.class))),
        @ApiResponse(responseCode = "404", description = "Not found")})
    ResponseEntity<CmsConsent> getConsentById(
        @Parameter(name = "encrypted-consent-id",
            description = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId);

    @GetMapping(path = "/{encrypted-consent-id}/status")
    @Operation(description = "Get consent status by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ConsentStatus.class))),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<ConsentStatusResponse> getConsentStatusById(
        @Parameter(name = "encrypted-consent-id",
            description = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId);

    @PutMapping(path = "/{encrypted-consent-id}/status/{status}")
    @Operation(description = "Update consent status by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Checksum verification failed"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Object> updateConsentStatus(
        @Parameter(name = "encrypted-consent-id",
            description = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @Parameter(description = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.",
            example = "VALID",
            required = true)
        @PathVariable("status") String status);

    @DeleteMapping(path = "/{encrypted-consent-id}/old-consents")
    @Operation(description = "Find and terminate old consents for TPP and PSU by new consent ID")
    @ApiResponse(responseCode = "204", description = "No Content")
    ResponseEntity<Void> findAndTerminateOldConsentsByNewConsentId(
        @Parameter(name = "encrypted-consent-id",
            description = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId);

    @PutMapping(path = "/{encrypted-consent-id}/old-consents")
    @Operation(description = "Find and terminate old consents for TPP and PSU by new consent ID")
    @ApiResponse(responseCode = "204", description = "No Content")
    ResponseEntity<Void> findAndTerminateOldConsents(
        @Parameter(name = "encrypted-consent-id",
            description = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @RequestBody TerminateOldConsentsRequest cmsTerminateConsentsRequest);

    @PutMapping(path = "/{encrypted-consent-id}/multilevel-sca")
    @Operation(description = "Update requirement for multilevel SCA for consent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Checksum verification failed"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Object> updateMultilevelScaRequired(
        @Parameter(name = "encrypted-consent-id", description = "Encrypted consent ID", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7", required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @Parameter(name = "multilevel-sca", description = "Multilevel SCA", example = "false")
        @RequestParam(value = "multilevel-sca", defaultValue = "false") boolean multilevelSca);
}
