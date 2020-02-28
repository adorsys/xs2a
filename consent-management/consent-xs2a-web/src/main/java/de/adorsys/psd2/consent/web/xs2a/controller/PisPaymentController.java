/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.consent.web.xs2a.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/pis")
@Api(value = "api/v1/pis", tags = InternalCmsXs2aApiTagName.PIS_PAYMENTS)
public class PisPaymentController {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentService;
    private final UpdatePaymentAfterSpiServiceEncrypted updatePaymentStatusAfterSpiService;

    @GetMapping(path = "/payment/{payment-id}")
    @ApiOperation(value = "Get inner payment id by encrypted string")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<String> getPaymentIdByEncryptedString(
        @ApiParam(name = "payment-id",
            value = "The payment identification.",
            example = "32454656712432",
            required = true)
        @PathVariable("payment-id") String encryptedId) {
        CmsResponse<String> response = pisCommonPaymentService.getDecryptedId(encryptedId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @PutMapping(path = "/payment/{payment-id}/status/{status}")
    @ApiOperation(value = "Updates payment status after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Void> updatePaymentStatusAfterSpiService(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "The following code values are permitted 'ACCC', 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'ACWC', 'ACWP', 'PDNG', 'RJCT', 'RCVD', 'CANC', 'ACFC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "ACCC, ACCP, ACSC, ACSP, ACTC, ACWC, ACWP, RCVD, PDNG, RJCT, CANC, ACFC, PATC",
            required = true)
        @PathVariable("status") String status) {
        CmsResponse<Boolean> response = updatePaymentStatusAfterSpiService.updatePaymentStatus(paymentId, TransactionStatus.valueOf(status));

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PutMapping(path = "/payment/{payment-id}/cancellation/redirects")
    @ApiOperation(value = "Updates payment cancellation redirect URIs after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Void> updatePaymentCancellationTppRedirectUri(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "TPP-Redirect-URI", required = false) String tpPRedirectURI,
        @RequestHeader(value = "TPP-Nok-Redirect-URI", required = false) String tpPNokRedirectURI) {
        CmsResponse<Boolean> response = updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(paymentId,
                                                                                                                   new TppRedirectUri(StringUtils.defaultIfBlank(tpPRedirectURI, ""),
                                                                                                                                      StringUtils.defaultIfBlank(tpPNokRedirectURI, "")));
        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PutMapping(path = "/payment/{payment-id}/cancellation/internal-request-id/{internal-request-id}")
    @ApiOperation(value = "Updates payment cancellation internal request ID after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Void> updatePaymentCancellationInternalRequestId(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(name = "internal-request-id",
            value = "Cancellation internal request ID of payment.",
            required = true)
        @PathVariable("internal-request-id") String internalRequestId) {
        CmsResponse<Boolean> response = updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(paymentId, internalRequestId);

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
