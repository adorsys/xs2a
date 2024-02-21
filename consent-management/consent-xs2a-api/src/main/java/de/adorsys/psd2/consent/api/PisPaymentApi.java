/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "api/v1/pis")
@Tag(name = InternalCmsXs2aApiTagName.PIS_PAYMENTS, description = "Provides access to consent management system for PIS")
public interface PisPaymentApi {

    @GetMapping(path = "/payment/{payment-id}")
    @Operation(description = "Get inner payment ID by encrypted string")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<String> getPaymentIdByEncryptedString(
        @Parameter(name = "payment-id",
            description = "The payment identification",
            example = "32454656712432",
            required = true)
        @PathVariable("payment-id") String encryptedId);

    @PutMapping(path = "/payment/{payment-id}/status/{status}")
    @Operation(description = "Updates payment status after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    ResponseEntity<Void> updatePaymentStatusAfterSpiService(
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @Schema(description = "The following code values are permitted 'ACCC', 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'ACWC', 'ACWP', 'PDNG', 'RJCT', 'RCVD', 'CANC', 'ACFC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "ACCC, ACCP, ACSC, ACSP, ACTC, ACWC, ACWP, RCVD, PDNG, RJCT, CANC, ACFC, PATC",
            required = true)
        @PathVariable("status") String status);

    @PutMapping(path = "/payment/{payment-id}/internal-status/{status}")
    @Operation(description = "Updates payment status after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    ResponseEntity<Void> updateInternalPaymentStatusAfterSpiService(
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment.",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @Schema(description = "The following code values are permitted 'INITIATED', 'CANCELLED_INITIATED', 'CANCELLED_FINALISED', 'FINALISED', 'REJECTED'. These values might be extended by ASPSP by more values.",
            allowableValues = "INITIATED, CANCELLED_INITIATED, CANCELLED_FINALISED, FINALISED, REJECTED",
            required = true)
        @PathVariable("status") String status);

    @PutMapping(path = "/payment/{payment-id}/cancellation/redirects")
    @Operation(description = "Updates payment cancellation redirect URIs after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    ResponseEntity<Void> updatePaymentCancellationTppRedirectUri(
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "TPP-Redirect-URI", required = false) String tpPRedirectURI,
        @RequestHeader(value = "TPP-Nok-Redirect-URI", required = false) String tpPNokRedirectURI);

    @PutMapping(path = "/payment/{payment-id}/cancellation/internal-request-id/{internal-request-id}")
    @Operation(description = "Updates payment cancellation internal request ID after SPI service. Should not be used for any other purposes!")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    ResponseEntity<Void> updatePaymentCancellationInternalRequestId(
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @Parameter(name = "internal-request-id",
            description = "Cancellation internal request ID of payment",
            required = true)
        @PathVariable("internal-request-id") String internalRequestId);
}
