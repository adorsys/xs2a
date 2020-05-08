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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "api/v1/pis")
@Api(value = "api/v1/pis", tags = InternalCmsXs2aApiTagName.PIS_PAYMENTS)
public interface PisPaymentApi {

    @GetMapping(path = "/payment/{payment-id}")
    @ApiOperation(value = "Get inner payment id by encrypted string")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<String> getPaymentIdByEncryptedString(
        @ApiParam(name = "payment-id",
            value = "The payment identification.",
            example = "32454656712432",
            required = true)
        @PathVariable("payment-id") String encryptedId);

    @PutMapping(path = "/payment/{payment-id}/status/{status}")
    @ApiOperation(value = "Updates payment status after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request")})
    ResponseEntity<Void> updatePaymentStatusAfterSpiService(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "The following code values are permitted 'ACCC', 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'ACWC', 'ACWP', 'PDNG', 'RJCT', 'RCVD', 'CANC', 'ACFC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "ACCC, ACCP, ACSC, ACSP, ACTC, ACWC, ACWP, RCVD, PDNG, RJCT, CANC, ACFC, PATC",
            required = true)
        @PathVariable("status") String status);

    @PutMapping(path = "/payment/{payment-id}/cancellation/redirects")
    @ApiOperation(value = "Updates payment cancellation redirect URIs after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request")})
    ResponseEntity<Void> updatePaymentCancellationTppRedirectUri(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "TPP-Redirect-URI", required = false) String tpPRedirectURI,
        @RequestHeader(value = "TPP-Nok-Redirect-URI", required = false) String tpPNokRedirectURI);

    @PutMapping(path = "/payment/{payment-id}/cancellation/internal-request-id/{internal-request-id}")
    @ApiOperation(value = "Updates payment cancellation internal request ID after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request")})
    ResponseEntity<Void> updatePaymentCancellationInternalRequestId(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(name = "internal-request-id",
            value = "Cancellation internal request ID of payment.",
            required = true)
        @PathVariable("internal-request-id") String internalRequestId);
}
