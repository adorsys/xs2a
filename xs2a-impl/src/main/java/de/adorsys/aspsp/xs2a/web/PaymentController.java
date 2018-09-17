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

import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.service.PaymentService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/")
@Api(value = "api/v1/", tags = "PISP, Access to Payments", description = "Provides access to the PIS payments")
public class PaymentController {
    private final ResponseMapper responseMapper;
    private final PaymentService paymentService;

    @ApiOperation(value = "Get payment information")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Xs2aTransactionStatus.class),
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 403, message = "Wrong path variables")})
    @GetMapping(path = "/{payment-service}/{payment-product}/{paymentId}")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "x-request-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "date", value = "Sun, 11 Aug 2019 15:02:37 GMT", required = true, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-ip-type", value = "no data", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-qwac-certificate", value = "qwac certificate", required = true, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "digest of the payload request", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "psu-ip-address", value = "192.168.0.26", required = true, dataType = "String", paramType = "header")})//NOPMD //Ip is required as description of the field
    public ResponseEntity getPaymentById(
        @ApiParam(name = "payment-service", value = "The addressed payment service", required = true, allowableValues = "payments, bulk-payments,periodic-payments")
        @PathVariable("payment-service") PaymentType paymentType,
        @ApiParam(name = "payment-product", value = "The addressed payment product ", required = true, allowableValues = "sepa-credit-transfers, target-2-payments, instant-sepa-credit-transfers, cross-border-credit-transfers")
        @PathVariable("payment-product") String paymentProduct,
        @ApiParam(name = "paymentId", value = "529e0507-7539-4a65-9b74-bdf87061e99b", required = true)
        @PathVariable("paymentId") String paymentId) {
        return responseMapper.ok(paymentService.getPaymentById(paymentType, paymentId));
    }
}
