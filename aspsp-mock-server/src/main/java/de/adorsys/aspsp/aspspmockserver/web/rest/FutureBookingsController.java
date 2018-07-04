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

package de.adorsys.aspsp.aspspmockserver.web.rest;

import de.adorsys.aspsp.aspspmockserver.service.FutureBookingsService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/future-bookings")
@Api(tags = "Future payments", description = "Provides access to future payment execution")
public class FutureBookingsController {
    private final FutureBookingsService futureBookingsService;

    @ApiOperation(value = "Executes future payments for account specified by IBAN and Currency, with update on corresponding account balances", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SpiAccountDetails.class),
        @ApiResponse(code = 204, message = "No Content")})
    @PostMapping(path = "/{iban}/{currency}")
    public ResponseEntity<SpiAccountDetails> changeBalances(@PathVariable("iban") String iban, @PathVariable("currency") String currency) {
        return futureBookingsService.changeBalances(iban, currency)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.noContent().build());
    }
}
