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
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.ACCP;
import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.RJCT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.util.CollectionUtils.isEmpty;

@RestController
@RequestMapping(path = "/payments")
public class PaymentController {
    private PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @PostMapping(path = "/")
    public ResponseEntity<SpiSinglePayments> createPayment(@RequestBody SpiSinglePayments payment) throws Exception {
        return paymentService.addPayment(payment)
            .map(saved -> new ResponseEntity<>(saved, CREATED))
            .orElse(ResponseEntity.badRequest().build());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @PostMapping(path = "/bulk-payments/")
    public ResponseEntity<List<SpiSinglePayments>> createBulkPayments(
        @RequestBody List<SpiSinglePayments> payments) throws Exception {
        List<SpiSinglePayments> saved = paymentService.addBulkPayments(payments);
        return isEmpty(saved)
            ? ResponseEntity.badRequest().build()
            : new ResponseEntity<>(saved, CREATED);
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{paymentId}/status/")
    public ResponseEntity getPaymentStatusById(@PathVariable("paymentId") String paymentId) {
        return paymentService.isPaymentExist(paymentId)
            ? ResponseEntity.ok(ACCP) : ResponseEntity.ok(RJCT);
    }
}
