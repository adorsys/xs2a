/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.web.xs2a;

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/aspsp-consent-data")
@Api(value = "api/v1/aspsp-consent-data", tags = "Aspsp Consent Data", description = "Provides access to consent management system for AspspDataConsent")
public class AspspConsentDataController {
    private final AspspDataService aspspDataService;

    @GetMapping(path = "/consent/{consent-id}")
    @ApiOperation(value = "Get aspsp consent data identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CmsAspspConsentDataBase64> getAspspConsentDataByConsentId(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI")
        @PathVariable("consent-id") String encryptedConsentId) {
        return aspspDataService.readAspspConsentData(encryptedConsentId)
            .map(AspspConsentData::getAspspConsentData)
            .map(Base64.getEncoder()::encodeToString)
            .map(aspspConsentDataBase64 -> new CmsAspspConsentDataBase64(encryptedConsentId, aspspConsentDataBase64))
            .map(cmsAspspConsentDataBase64 -> new ResponseEntity<>(cmsAspspConsentDataBase64, HttpStatus.OK))
            .orElseGet(ResponseEntity.notFound()::build);

    }

    @GetMapping(path = "/payment/{payment-id}")
    @ApiOperation(value = "Get aspsp consent data identified by given payment id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CmsAspspConsentDataBase64> getAspspConsentDataByPaymentId(
        @ApiParam(name = "payment-id", value = "The payment identification.", example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI")
        @PathVariable("payment-id") String encryptedPaymentId) {
        return aspspDataService.readAspspConsentData(encryptedPaymentId)
                   .map(AspspConsentData::getAspspConsentData)
                   .map(Base64.getEncoder()::encodeToString)
                   .map(aspspConsentDataBase64 -> new CmsAspspConsentDataBase64(encryptedPaymentId, aspspConsentDataBase64))
                   .map(cmsAspspConsentDataBase64 -> new ResponseEntity<>(cmsAspspConsentDataBase64, HttpStatus.OK))
                   .orElseGet(ResponseEntity.notFound()::build);
    }

    @PutMapping(path = "/consent/{consent-id}")
    @ApiOperation(value = "Update aspsp consent data identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity updateAspspConsentData(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI")
        @PathVariable("consent-id") String encryptedConsentId,
        @RequestBody CmsAspspConsentDataBase64 request) {
        byte[] data = Optional.ofNullable(request.getAspspConsentDataBase64())
                          .map(Base64.getDecoder()::decode)
                          .orElse(null);

        AspspConsentData aspspConsentData = new AspspConsentData(data, encryptedConsentId);
        boolean updated = aspspDataService.updateAspspConsentData(aspspConsentData);

        if (!updated) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/consent/{consent-id}")
    @ApiOperation(value = "Delete aspsp consent data identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity deleteAspspConsentDataByConsentId(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "CxymMkwtykFtTeQuH1jrcoOyzcqCcwNCt5193Gfn33mqqcAy_xw2KPwMd5y6Xxe1EwE0BTNRHeyM0FI90wh0hA==_=_bS6p6XvTWI")
        @PathVariable("consent-id") String encryptedConsentId) {
        boolean deleted = aspspDataService.deleteAspspConsentData(encryptedConsentId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
