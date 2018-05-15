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

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/account")
public class AccountController {
    private AccountService accountService;

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/")
    public ResponseEntity<List<SpiAccountDetails>> readAllAccounts() {
        return Optional.of(accountService.getAllAccounts())
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{accountId}")
    public ResponseEntity<SpiAccountDetails> readAccountById(@PathVariable("accountId") String accountId) {
        return accountService.getAccount(accountId)
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @PutMapping(path = "/")
    public ResponseEntity createAccount(HttpServletRequest request,
                                        @RequestBody SpiAccountDetails account) throws Exception {
       /* String uriString = getUriString(request);
        SpiAccountDetails saved = accountService.addOrUpdateAccount(account);
        return ResponseEntity.created(new URI(uriString + saved.getId())).build();*/ //TODO to be refactored by May
        return null;
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @DeleteMapping(path = "/{accountId}")
    public ResponseEntity deleteAccount(@PathVariable("accountId") String accountId) {
        /*if (accountService.deleteAccountById(accountId)) {
            return ResponseEntity.noContent().build();
        }*/ //TODO to be refactore by May
        return ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{accountId}/balances")
    public ResponseEntity<List<SpiBalances>> readBalancesById(@PathVariable("accountId") String accountId) {
        return null/*accountService.getBalances(accountId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build())*/;  //TODO to be refactored by DMI
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{psuId}/accountsByPsuId")
    public ResponseEntity<SpiAccountDetails[]> readAccountsByPsuId(@PathVariable("psuId") String psuId) {
        return Optional.of(accountService.getAccountsByPsuId(psuId))
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{iban}/accountsByIban")
    public ResponseEntity<List<SpiAccountDetails>> readAccountsByIban(@PathVariable("iban") String iban) {
        return Optional.of(accountService.getAccountsByIban(iban))
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
