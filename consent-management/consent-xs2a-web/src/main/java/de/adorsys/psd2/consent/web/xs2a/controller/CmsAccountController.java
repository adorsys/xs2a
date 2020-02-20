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


import de.adorsys.psd2.consent.api.service.AccountServiceEncrypted;
import de.adorsys.psd2.consent.web.xs2a.config.InternalCmsXs2aApiTagName;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/ais/consent")
@Api(value = "api/v1/ais/consent", tags = InternalCmsXs2aApiTagName.AIS_CONSENTS)
public class CmsAccountController {
    private final AccountServiceEncrypted accountServiceEncrypted;

    @PutMapping(path = "/{encrypted-consent-id}/{resource-id}")
    @ApiOperation(value = "Saves number of transactions for a definite account of the definite consent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> saveNumberOfTransactions(
        @ApiParam(name = "consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @PathVariable("resource-id") String resourceId,
        @RequestBody Integer numberOfTransactions) {
        return accountServiceEncrypted.saveNumberOfTransactions(encryptedConsentId, resourceId, numberOfTransactions)
                   ? new ResponseEntity<>(true, HttpStatus.OK)
                   : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
