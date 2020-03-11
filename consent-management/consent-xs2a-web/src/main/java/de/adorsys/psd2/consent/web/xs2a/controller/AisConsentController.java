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
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.UpdateAisConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.web.xs2a.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.core.data.AccountAccess;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/ais/consent")
@Api(value = "api/v1/ais/consent", tags = InternalCmsXs2aApiTagName.AIS_CONSENTS)
public class AisConsentController {
    private final AisConsentServiceEncrypted aisConsentService;

    @PostMapping(path = "/action")
    @ApiOperation(value = "Save information about uses of consent")
    public ResponseEntity<Object> saveConsentActionLog(@RequestBody AisConsentActionRequest request) {

        try {
            aisConsentService.checkConsentAndSaveActionLog(request);
        } catch (WrongChecksumException e) {
            return new ResponseEntity<>(CmsError.CHECKSUM_ERROR, HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/{encrypted-consent-id}/access")
    @ApiOperation(value = "Update AccountAccess in the consent identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Checksum verification failed"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Object> updateAccountAccess(
        @ApiParam(name = "consent-id",
            value = "The account consent identification assigned to the created account consent.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @RequestBody AccountAccess request) {
        CmsResponse<CmsConsent> response;

        try {
            response = aisConsentService.updateAspspAccountAccess(encryptedConsentId, request);
        } catch (WrongChecksumException e) {
            return new ResponseEntity<>(CmsError.CHECKSUM_ERROR, HttpStatus.BAD_REQUEST);
        }

        if (response.hasError()) {
            return new ResponseEntity<>(response.getError(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new UpdateAisConsentResponse(response.getPayload()), HttpStatus.OK);
    }
}
