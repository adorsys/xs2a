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

import de.adorsys.aspsp.aspspmockserver.service.TransactionService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.Date;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/")
    public ResponseEntity<List<SpiTransaction>> readAllTransactions() {
        List<SpiTransaction> transactions = transactionService.getAllTransactions();
        return CollectionUtils.isEmpty(transactions)
                   ? ResponseEntity.notFound().build()
                   : ResponseEntity.ok(transactions);
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{transaction-id}")
    public ResponseEntity<SpiTransaction> readTransactionById(@PathVariable("transaction-id") String transactionId) {
        return transactionService.getTransactionById(transactionId)
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @PostMapping(path = "/")
    public ResponseEntity createTransaction(@RequestBody SpiTransaction transaction) {
        return transactionService.saveTransaction(transaction)
                   .map(transactionId -> new ResponseEntity<>(transactionId, CREATED))
                   .orElse(ResponseEntity.badRequest().build());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{iban}/{currency}")
    public ResponseEntity<List<SpiTransaction>> readTransactionsByPeriod(@PathVariable("iban") String iban, @PathVariable("currency") Currency currency,
                                                                         @RequestParam(value = "dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateFrom,
                                                                         @RequestParam(value = "dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateTo) {
        List<SpiTransaction> response = transactionService.getTransactionsByPeriod(iban, currency, dateFrom, dateTo);
        return CollectionUtils.isEmpty(response)
                   ? ResponseEntity.notFound().build()
                   : ResponseEntity.ok(response);
    }
}
