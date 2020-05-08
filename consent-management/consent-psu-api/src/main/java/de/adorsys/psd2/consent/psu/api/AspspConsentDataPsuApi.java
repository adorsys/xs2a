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

import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.adorsys.psd2.consent.psu.api.config.CmsPsuApiTagName.ASPSP_CONSENT_DATA;

@RequestMapping(path = "psu-api/v1/aspsp-consent-data/consents/{consent-id}")
@Api(value = "psu-api/v1/aspsp-consent-data", tags = ASPSP_CONSENT_DATA)
public interface AspspConsentDataPsuApi {

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
    ResponseEntity<Void> updateAspspConsentData(
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
    ResponseEntity<Void> deleteAspspConsentData(
        @ApiParam(
            name = "consent-id",
            value = "The account consent identification assigned to the created account consent / payment identification assigned to the created payment.",
            example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI",
            required = true)
        @PathVariable("consent-id") String encryptedConsentId);
}
