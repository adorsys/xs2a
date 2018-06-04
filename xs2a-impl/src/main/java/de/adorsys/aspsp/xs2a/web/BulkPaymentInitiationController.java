
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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/bulk-payments/{payment-product}")
@Api(value = "api/v1/consents", tags = "PISP, Bulk payment initiation", description = "Payment Initiation for Bulk Payments and Multiple Payments")
public class BulkPaymentInitiationController {
    private final ResponseMapper responseMapper;
    private final PaymentService paymentService;

    @ApiOperation(value = "Creates a bulk payment initiation request at the ASPSP", authorizations = { @Authorization(value="oauth2", scopes = { @AuthorizationScope(scope = "read", description = "Access read API") }) })
    @ApiResponses(value = {@ApiResponse(code = 201, message = "transactions_status received, a list of hyperlinks to be recognized by the Tpp."),
    @ApiResponse(code = 400, message = "Bad request")})
    @PostMapping()
    @ApiImplicitParams({
    @ApiImplicitParam(name = "psu-ip-address", value = "192.168.0.26", required = true, paramType = "header"), //NOPMD value is correct according to specification
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<List<PaymentInitialisationResponse>> createBulkPaymentInitiation(
    @ApiParam(name = "payment-product", value = "The addressed payment product endpoint for bulk payments e.g. for a bulk SEPA Credit Transfers", allowableValues = "sepa-credit-transfers, target-2-payments,instant-sepa-credit-transfers, cross-border-credit-transfers")
    @PathVariable("payment-product") String paymentProduct,
    @ApiParam(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @RequestParam(name = "tppRedirectPreferred", required = false) boolean tppRedirectPreferred,
    @RequestBody List<SinglePayments> payments) {
        return responseMapper.created(paymentService.createBulkPayments(payments, PaymentProduct.forValue(paymentProduct), tppRedirectPreferred));
    }
}
