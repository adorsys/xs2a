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

import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/payments/{payment-product}")
@Api(value = "api/v1/payments/{payment-product}", tags = "PISP Payments", description = "Provides access to the PIS")
public class PaymentInitiationController {

    private final ResponseMapper responseMapper;
    private final PaymentService paymentService;

    @ApiOperation(value = "Initialises a new payment ", notes = "debtor account, creditor accout, creditor name, remittance information unstructured")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "transactions_status received, a list of hyperlinks to be recognized by the Tpp."),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(method = RequestMethod.POST)
    public PaymentInitialisationResponse createPaymentInitiation(
    @ApiParam(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @RequestParam(name = "tppRedirectPreferred", required = false) boolean tppRedirectPreferred,
    @RequestBody SinglePayments paymentInitialisationRequest) {
        // TODO according task PIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/9
        return null;
    }

    @ApiOperation(value = "Get information  about the status of a payment initialisation ")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "transactions_status Accepted Customer Profile.", response = Map.class),
    @ApiResponse(code = 404, message = "Not found")})
    @RequestMapping(value = "/{paymentId}/status", method = RequestMethod.GET)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "authorization bearer", value = "Some bearer", required = false, dataType = "String", paramType = "header"),
    @ApiImplicitParam(name = "signature", value = "98c0", required = false, dataType = "String", paramType = "header"),
    @ApiImplicitParam(name = "tpp-certificate", value = "some certificate", required = false, dataType = "String", paramType = "header")})
    public ResponseEntity<Map<String, TransactionStatus>> getPaymentInitiationStatusById(
    @ApiParam(name = "payment-product", value = "The addressed payment product endpoint for bulk payments e.g. for a bulk SEPA Credit Transfers", allowableValues = "sepa-credit-transfers, target-2-payments,instant-sepa-credit-transfers, cross-border-credit-transfers")
    @PathVariable("payment-product") String paymentProduct,
    @ApiParam(name = "paymentId", value = "529e0507-7539-4a65-9b74-bdf87061e99b")
    @PathVariable("paymentId") String paymentId) {
        return responseMapper.okOrNotFound(paymentService.getPaymentStatusById(paymentId, PaymentProduct.forValue(paymentProduct)));
    }
}
