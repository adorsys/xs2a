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
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/periodic-payments/{payment-product}")
@Api(value = "api/v1/periodic-payments/{payment-product}", tags = "PISP, Periodic Payments", description = "Orders for periodic payments")
public class PeriodicPaymentsController {
    private final PaymentService paymentService;
    private final ResponseMapper responseMapper;

    @ApiOperation(value = "The TPP can submit a recurring payment initiation where the starting date, frequency and conditionally an end date is provided. Once authorised by the PSU, the payment then will be executed by the ASPSP, if possible, following this “standing order” as submitted by the TPP.", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Created"),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(method = RequestMethod.POST)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "psu-ip-address", value = "192.168.0.26", required = true, paramType = "header"),//NOPMD.AvoidUsingHardCodedIP ignored because the value is right according to documentation
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<PaymentInitialisationResponse> createPeriodicPayment(
    @ApiParam(name = "payment-product", value = "The addressed payment product endpoint for periodic payments e.g. for a periodic SEPA Credit Transfers", allowableValues = "sepa-credit-transfers, target-2-payments,instant-sepa-credit-transfers, cross-border-credit-transfers", required = true)
    @PathVariable("payment-product") String paymentProduct,
    @ApiParam(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @RequestParam(name = "tppRedirectPreferred", required = false) boolean tppRedirectPreferred,
    @ApiParam(name = "Periodic Payment", value = "All data relevant for the corresponding payment product and necessary for execution of the standing order.", required = true)
    @RequestBody PeriodicPayment periodicPayment) {
        return responseMapper.createdOrBadRequest(paymentService.initiatePeriodicPayment(periodicPayment, PaymentProduct.forValue(paymentProduct), tppRedirectPreferred));
    }
}
