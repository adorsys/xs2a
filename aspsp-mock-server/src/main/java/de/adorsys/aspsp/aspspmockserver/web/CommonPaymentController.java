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

package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspPaymentInfo;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/payments/common")
@Api(tags = "Common payments", description = "Provides access to common payments")
public class CommonPaymentController {
    private final PaymentService paymentService;

    @ApiOperation(value = "Creates a payment based on request body", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = AspspPaymentInfo.class),
        @ApiResponse(code = 204, message = "Payment Failed")})
    @PostMapping()
    public ResponseEntity<AspspPaymentInfo> createPayment(@RequestBody AspspPaymentInfo aspspPaymentInfo) {
        return paymentService.addPaymentInfo(aspspPaymentInfo)
                   .map(saved -> new ResponseEntity<>(saved, CREATED))
                   .orElseGet(ResponseEntity.noContent()::build);
    }

    @ApiOperation(value = "Returns the payment requested by it`s ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AspspPaymentInfo.class),
        @ApiResponse(code = 404, message = "Payment Not Found")})
    @GetMapping(path = "/{paymentId}")
    public ResponseEntity<AspspPaymentInfo> getPaymentById(@PathVariable("paymentId") String paymentId) {
        Optional<AspspPaymentInfo> paymentOpt = paymentService.getCommonPaymentById(paymentId);
        return paymentOpt.isPresent()
                   ? ResponseEntity.ok(paymentOpt.get())
                   : ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "Returns the status of payment requested by it`s ASPSP identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AspspTransactionStatus.class),
        @ApiResponse(code = 404, message = "Payment Not Found")})
    @GetMapping(path = "/{paymentId}/status")
    public ResponseEntity<AspspTransactionStatus> getPaymentStatusById(@PathVariable("paymentId") String paymentId) {
        return paymentService.getPaymentStatusById(paymentId)
                   .map(ResponseEntity::ok)
                   .orElseGet(ResponseEntity.noContent()::build);
    }
}
