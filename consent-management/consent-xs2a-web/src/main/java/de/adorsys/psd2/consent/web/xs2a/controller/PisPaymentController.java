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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.consent.api.service.UpdatePaymentStatusAfterSpiService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/pis")
@Api(value = "api/v1/pis", tags = "PIS, Payments", description = "Provides access to consent management system for PIS")
public class PisPaymentController {
    private final PisConsentService pisConsentService;
    private final UpdatePaymentStatusAfterSpiService updatePaymentStatusAfterSpiService;

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

    @PutMapping(path = "/payment/{payment-id}/status/{status}")
    @ApiOperation(value = "Updates payment status after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Void> updatePaymentStatusAfterSpiService(
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created payment.")
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "The following code values are permitted 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'PDNG', 'RCVD', 'RJCT', 'CANC'. These values might be extended by ASPSP by more values.", allowableValues = "ACCP,  ACSC, ACSP, ACTC, ACWC, ACWP, RCVD, PDNG, RJCT, CANC")
        @PathVariable("status") String status) {
        return updatePaymentStatusAfterSpiService.updatePaymentStatus(paymentId, TransactionStatus.valueOf(status))
                   ? ResponseEntity.ok().build()
                   : ResponseEntity.badRequest().build();
    }
}
