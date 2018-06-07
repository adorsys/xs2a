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

import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.service.FundsConfirmationService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/funds-confirmations")
@Api(value = "api/v1/funds-confirmations", tags = "PIISP, Funds confirmation", description = "Provides access to the funds confirmation")
public class FundsConfirmationController {
    private final FundsConfirmationService fundsConfirmationService;
    private final ResponseMapper responseMapper;

    @PostMapping
    @ApiOperation(value = "Create a confirmation of funds request ", notes = "debtor account, creditor account, creditor name, remittance information unstructured", authorizations = { @Authorization(value="oauth2", scopes = { @AuthorizationScope(scope = "read", description = "Access read API") }) })
    @ApiResponses(value = {@ApiResponse(code = 200, message = "return true or false"), @ApiResponse(code = 400, message = "Bad request")})
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<FundsConfirmationResponse> fundConfirmation(@RequestBody FundsConfirmationRequest request) {

        return responseMapper.ok(fundsConfirmationService.fundsConfirmation(request));
    }
}
