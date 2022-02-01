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

import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "api/v1/aspsp-consent-data/consents/{consent-id}")
@Api(value = "api/v1/aspsp-consent-data", tags = InternalCmsXs2aApiTagName.ASPSP_CONSENT_DATA)
public interface AspspConsentDataApi {

    @GetMapping
    @ApiOperation(value = "Get aspsp consent data identified by given consent id / payment id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<CmsAspspConsentDataBase64> getAspspConsentData(
        @ApiParam(
            name = "consent-id",
            value = "The account consent identification assigned to the created account consent / payment identification assigned to the created payment.",
            example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI",
            required = true)
        @PathVariable("consent-id") String encryptedConsentId);

    @PutMapping
    @ApiOperation(value = "Update aspsp consent data identified by given consent id / payment id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<Boolean> updateAspspConsentData(
        @ApiParam(
            name = "consent-id",
            value = "The account consent identification assigned to the created account consent / payment identification assigned to the created payment.",
            example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI",
            required = true)
        @PathVariable("consent-id") String encryptedConsentId,
        @RequestBody CmsAspspConsentDataBase64 request);

    @DeleteMapping
    @ApiOperation(value = "Delete aspsp consent data identified by given consent id / payment id.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<Boolean> deleteAspspConsentData(
        @ApiParam(
            name = "consent-id",
            value = "The account consent identification assigned to the created account consent / payment identification assigned to the created payment.",
            example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI",
            required = true)
        @PathVariable("consent-id") String encryptedConsentId);
}
