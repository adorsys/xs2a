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
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentDataStatusResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "api/v1/pis/common-payments")
@Tag(name = InternalCmsXs2aApiTagName.PIS_COMMON_PAYMENT, description = "Provides access to common payment system for PIS")
public interface PisCommonPaymentApi {

    @PostMapping(path = "/")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CreatePisCommonPaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")})
    ResponseEntity<CreatePisCommonPaymentResponse> createCommonPayment(@RequestBody PisPaymentInfo request);

    @GetMapping(path = "/{payment-id}/status")
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PisCommonPaymentDataStatusResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")})
    ResponseEntity<PisCommonPaymentDataStatusResponse> getPisCommonPaymentStatusById(
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId);

    @GetMapping(path = "/{payment-id}")
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PisCommonPaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")})
    ResponseEntity<PisCommonPaymentResponse> getCommonPaymentById(
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId);

    @PutMapping(path = "/{payment-id}/status/{status}")
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad request")})
    ResponseEntity<Void> updateCommonPaymentStatus(
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @Schema(description = "The following code values are permitted 'ACCC', 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'ACWC', 'ACWP', 'PDNG', 'RJCT', 'RCVD', 'CANC', 'ACFC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "AcceptedSettlementCompletedCreditor, AcceptedCustomerProfile, AcceptedSettlementCompleted, AcceptedSettlementInProcess, AcceptedTechnicalValidation, AcceptedWithChange, AcceptedWithoutPosting, Received, Pending, Rejected, Canceled, AcceptedFundsChecked, PartiallyAcceptedTechnicalCorrect",
            required = true)
        @PathVariable("status") String status);

    @PutMapping(path = "/{payment-id}/multilevel-sca")
    @Operation(description = "Updates multilevel SCA required by payment ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Bad Request")})
    ResponseEntity<Boolean> updateMultilevelScaRequired(
        @Parameter(name = "payment-id",
            description = "The payment identification of the related payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable(name = "payment-id") String paymentId,
        @Parameter(name = "multilevel-sca", description = "Multilevel SCA", example = "false")
        @RequestParam(value = "multilevel-sca", defaultValue = "false") boolean multilevelSca);
}
