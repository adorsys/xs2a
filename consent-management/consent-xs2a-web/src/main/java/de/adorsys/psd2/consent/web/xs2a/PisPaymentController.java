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

import de.adorsys.psd2.consent.api.service.PisConsentService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/pis")
@Api(value = "api/v1/pis", tags = "PIS, Payments", description = "Provides access to consent management system for PIS")
public class PisPaymentController {
    private final PisConsentService pisConsentService;

    @GetMapping(path = "/payment/{payment-id}")
    @ApiOperation(value = "Get inner payment id by encrypted string")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<String> getPaymentIdByEncryptedString(
        @ApiParam(name = "payment-id", value = "The payment identification.", example = "32454656712432")
        @PathVariable("payment-id") String encryptedId) {
        return pisConsentService.getDecryptedId(encryptedId)
                   .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
