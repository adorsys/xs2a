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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.AllArgsConstructor;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/account")
public class AccountController {
    private AccountService accountService;

    @ApiOperation(value = "", authorizations = { @Authorization(value="oauth2", scopes = { @AuthorizationScope(scope = "read", description = "Access read API") }) })
    @GetMapping(path = "/")
    public ResponseEntity<List<SpiAccountDetails>> readAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<SpiAccountDetails> readAccountById(@PathVariable("id") String id) {
        return accountService.getAccount(id)
               .map(ResponseEntity::ok)
               .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ApiOperation(value = "", authorizations = { @Authorization(value="oauth2", scopes = { @AuthorizationScope(scope = "read", description = "Access read API") }) })
    @PutMapping(path = "/")
    public ResponseEntity createAccount(HttpServletRequest request,
                                        @RequestBody SpiAccountDetails account) throws Exception {
        String uriString = getUriString(request);
        SpiAccountDetails saved = accountService.addOrUpdateAccount(account);
        return ResponseEntity.created(new URI(uriString + saved.getId())).build();
    }

    private String getUriString(HttpServletRequest request) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUriString();
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity deleteAccount(@PathVariable("id") String id) {
        if (accountService.deleteAccountById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/{id}/balances")
    public ResponseEntity<List<SpiBalances>> readBalancesById(@PathVariable("id") String id) {
        return accountService.getBalances(id)
               .map(ResponseEntity::ok)
               .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
