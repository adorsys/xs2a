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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "api/v1/")
@Tag(name = InternalCmsXs2aApiTagName.AUTHORISATIONS, description = "Provides access to consent management system for authorisation endpoints")
public interface AuthorisationApi {

    @PostMapping(path = "/{authorisation-type}/{parent-id}/authorisations")
    @Operation(description = "Create consent authorization for given consent ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<CreateAuthorisationResponse> createConsentAuthorisation(
        @PathVariable("authorisation-type") AuthorisationType authorisationType,
        @PathVariable("parent-id") String parentId,
        @RequestBody CreateAuthorisationRequest authorisationRequest);

    @GetMapping(path = "/authorisations/{authorisation-id}")
    @Operation(description = "Getting consent authorisation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Authorisation> getAuthorisation(
        @Parameter(name = "authorisation-id",
            description = "The consent authorization identification assigned to the created authorization",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId);

    @PutMapping(path = "/authorisations/{authorisation-id}")
    @Operation(description = "Update consent authorisation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Void> updateAuthorisation(
        @Parameter(name = "authorization-id",
            description = "The consent authorisation identification assigned to the created authorisation",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestBody UpdateAuthorisationRequest authorisationRequest);

    @PutMapping(path = "/authorisations/{authorisation-id}/status/{status}")
    @Operation(description = "Update consent authorisation status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Void> updateAuthorisationStatus(
        @Parameter(name = "authorisation-id",
            description = "The consent authorisation identification assigned to the created authorisation",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @Parameter(description = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.",
            example = "VALID",
            required = true)
        @PathVariable("status") String scaStatus);

    @GetMapping(path = "/{authorisation-type}/{parent-id}/authorisations/{authorisation-id}/status")
    @Operation(description = "Gets SCA status of consent authorisation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<ScaStatus> getAuthorisationScaStatus(
        @PathVariable("authorisation-type") AuthorisationType authorisationType,
        @PathVariable("parent-id") String parentId,
        @PathVariable("authorisation-id") String authorisationId);

    @GetMapping(path = "/{authorisation-type}/{parent-id}/authorisations")
    @Operation(description = "Gets list of consent authorisation IDs by consent ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<List<String>> getAuthorisationsByParentId(
        @PathVariable("authorisation-type") AuthorisationType authorisationType,
        @PathVariable("parent-id") String parentId);

    @GetMapping(path = "/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}")
    @Operation(description = "Checks if requested authentication method is decoupled")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<Boolean> isAuthenticationMethodDecoupled(
        @Parameter(name = "authorisation-id",
            description = "Consent authorisation identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @Parameter(name = "authentication-method-id",
            description = "Authentication method identification",
            example = "sms",
            required = true)
        @PathVariable("authentication-method-id") String authenticationMethodId);

    @PostMapping(path = "/authorisations/{authorisation-id}/authentication-methods")
    @Operation(description = "Saves authentication methods in authorisation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Void> saveAuthenticationMethods(
        @Parameter(name = "authorisation-id",
            description = "The consent authorisation identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestBody List<CmsScaMethod> methods);

    @PutMapping(path = "/authorisations/{authorisation-id}/sca-approach/{sca-approach}")
    @Operation(description = "Updates AIS SCA approach in authorisation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Boolean> updateScaApproach(
        @Parameter(name = "authorisation-id",
            description = "The consent authorisation identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @Parameter(name = "sca-approach",
            description = "Chosen SCA approach",
            example = "REDIRECT",
            required = true)
        @PathVariable("sca-approach") ScaApproach scaApproach);

    @GetMapping(path = "/authorisations/{authorisation-id}/sca-approach")
    @Operation(description = "Gets SCA approach of the consent authorisation by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<AuthorisationScaApproachResponse> getAuthorisationScaApproach(
        @Parameter(name = "authorisation-id",
            description = "The consent authorisation identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId);
}
