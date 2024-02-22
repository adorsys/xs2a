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

import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "api/v1/aspsp-consent-data/consents/{consent-id}")
@Tag(name = InternalCmsXs2aApiTagName.ASPSP_CONSENT_DATA, description = "Provides access to consent management system for AspspDataConsent")
public interface AspspConsentDataApi {

    @GetMapping
    @Operation(description = "Get ASPSP consent data identified by given consent ID / payment ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<CmsAspspConsentDataBase64> getAspspConsentData(
        @Parameter(
            name = "consent-id",
            description = "The account consent identification assigned to the created account consent / payment identification assigned to the created payment",
            example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI",
            required = true)
        @PathVariable("consent-id") String encryptedConsentId);

    @PutMapping
    @Operation(description = "Update ASPSP consent data identified by given consent ID / payment ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Boolean> updateAspspConsentData(
        @Parameter(
            name = "consent-id",
            description = "The account consent identification assigned to the created account consent / payment identification assigned to the created payment",
            example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI",
            required = true)
        @PathVariable("consent-id") String encryptedConsentId,
        @RequestBody CmsAspspConsentDataBase64 request);

    @DeleteMapping
    @Operation(description = "Delete ASPSP consent data identified by given consent ID / payment ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Boolean> deleteAspspConsentData(
        @Parameter(
            name = "consent-id",
            description = "The account consent identification assigned to the created account consent / payment identification assigned to the created payment",
            example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI",
            required = true)
        @PathVariable("consent-id") String encryptedConsentId);
}
