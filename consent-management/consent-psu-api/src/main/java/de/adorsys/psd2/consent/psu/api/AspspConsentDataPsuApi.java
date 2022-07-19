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

import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.adorsys.psd2.consent.psu.api.config.CmsPsuApiTagName.ASPSP_CONSENT_DATA;

@RequestMapping(path = "psu-api/v1/aspsp-consent-data/consents/{consent-id}")
@Tag(name = ASPSP_CONSENT_DATA, description = "ASPSP Consent Data PSU API Controller")
public interface AspspConsentDataPsuApi {

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
    ResponseEntity<Void> updateAspspConsentData(
        @Parameter(
            name = "consent-id",
            description = "The account consent identification assigned to the created account consent / payment identification assigned to the created payment",
            example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI",
            required = true)
        @PathVariable("consent-id") String encryptedConsentId,
        @RequestBody CmsAspspConsentDataBase64 request);

    @DeleteMapping
    @Operation(description = "Delete aspsp consent data identified by given consent id / payment id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Void> deleteAspspConsentData(
        @Parameter(
            name = "consent-id",
            description = "The account consent identification assigned to the created account consent / payment identification assigned to the created payment",
            example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI",
            required = true)
        @PathVariable("consent-id") String encryptedConsentId);
}
