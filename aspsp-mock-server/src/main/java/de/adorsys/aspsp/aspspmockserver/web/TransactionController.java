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
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/transaction")
@Api(tags = "Transactions", description = "Provides access to transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @ApiOperation(value = "Returns a list of all transactions available at ASPSP", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = List.class),
        @ApiResponse(code = 404, message = "Not Found")})
    @GetMapping(path = "/")
    public ResponseEntity<List<SpiTransaction>> readAllTransactions() {
        List<SpiTransaction> transactions = transactionService.getAllTransactions();
        return CollectionUtils.isEmpty(transactions)
                   ? ResponseEntity.notFound().build()
                   : ResponseEntity.ok(transactions);
    }

    @ApiOperation(value = "Returns a transaction by its ASPSP identifier and account identifier", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SpiTransaction.class),
        @ApiResponse(code = 204, message = "No Content")})
    @GetMapping(path = "/{transaction-id}/{account-id}")
    public ResponseEntity<SpiTransaction> readTransactionById(@PathVariable("transaction-id") String transactionId, @PathVariable("account-id") String accountId) {
        return transactionService.getTransactionById(transactionId, accountId)
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @ApiOperation(value = "Creates a transaction at ASPSP", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping(path = "/")
    public ResponseEntity createTransaction(@RequestBody SpiTransaction transaction) {
        return transactionService.saveTransaction(transaction)
                   .map(transactionId -> new ResponseEntity<>(transactionId, CREATED))
                   .orElse(ResponseEntity.badRequest().build());
    }

    @ApiOperation(value = "Returns a list of transactions for account by its ASPSP identifier for a certain period of time bounded by dates from/to", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 204, message = "No Content")})
    @GetMapping(path = "/{account-id}")
    public ResponseEntity<List<SpiTransaction>> readTransactionsByPeriod(@PathVariable("account-id") String accountId,
                                                                         @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                                                         @RequestParam("dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        List<SpiTransaction> response = transactionService.getTransactionsByPeriod(accountId, dateFrom, dateTo);
        return CollectionUtils.isEmpty(response)
                   ? ResponseEntity.noContent().build()
                   : ResponseEntity.ok(response);
    }
}
