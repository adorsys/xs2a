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

import de.adorsys.aspsp.aspspmockserver.service.AccountService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.util.CollectionUtils.isEmpty;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/account")
public class AccountController {
    private final AccountService accountService;

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/")
    public ResponseEntity<List<SpiAccountDetails>> readAllAccounts() {
        return Optional.ofNullable(accountService.getAllAccounts())
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{accountId}")
    public ResponseEntity<SpiAccountDetails> readAccountById(@PathVariable("accountId") String accountId) {
        return accountService.getAccountById(accountId)
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @PutMapping(path = "/")
    public ResponseEntity createAccount(@RequestParam String psuId, @RequestBody SpiAccountDetails account) {
        return accountService.addAccount(psuId, account)
                   .map(acc -> new ResponseEntity<>(acc, CREATED))
                   .orElse(ResponseEntity.badRequest().build());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @DeleteMapping(path = "/{accountId}")
    public ResponseEntity deleteAccount(@PathVariable("accountId") String accountId) {
        accountService.deleteAccountById(accountId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{accountId}/balances")
    public ResponseEntity<List<SpiBalances>> readBalancesById(@PathVariable("accountId") String accountId) {
        List<SpiBalances> response = accountService.getAccountBalancesById(accountId);
        return isEmpty(response)
                   ? ResponseEntity.noContent().build()
                   : ResponseEntity.ok(response);
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/psu/{psuId}")
    public ResponseEntity<List<SpiAccountDetails>> readAccountsByPsuId(@PathVariable("psuId") String psuId) {
        List<SpiAccountDetails> response = accountService.getAccountsByPsuId(psuId);
        return isEmpty(response)
                   ? ResponseEntity.noContent().build()
                   : ResponseEntity.ok(response);
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/iban/{iban}")
    public ResponseEntity<List<SpiAccountDetails>> readAccountsByIban(@PathVariable("iban") String iban) {
        List<SpiAccountDetails> response = accountService.getAccountsByIban(iban);
        return isEmpty(response)
                   ? ResponseEntity.noContent().build()
                   : ResponseEntity.ok(response);
    }
}
